package fairteam_backend.config;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.filter.OncePerRequestFilter;

/** Sets the PostgreSQL tenant for the whole request transaction. */
public class TenantIsolationFilter extends OncePerRequestFilter {
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public TenantIsolationFilter(JdbcTemplate jdbcTemplate, PlatformTransactionManager transactionManager) {
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String requestPath = request.getRequestURI();
        if (requestPath.startsWith("/api/auth/") || requestPath.equals("/api/ai/workshops/generate")) {
            filterChain.doFilter(request, response);
            return;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            filterChain.doFilter(request, response);
            return;
        }

        long teacherId;
        try {
            teacherId = Long.parseLong(jwt.getSubject());
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Sesión FairTeam no válida.");
            return;
        }

        // A deactivated account loses access immediately, even with a token that has not expired yet.
        java.util.List<String> roles = jdbcTemplate.queryForList(
                "SELECT role FROM teacher_users WHERE id = ? AND active", String.class, teacherId);
        if (roles.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Cuenta desactivada o sesión no válida.");
            return;
        }
        // The administrator flag comes from the database, never from the token alone, and only
        // admin routes get the read-only cross-account policy.
        boolean adminRequest = requestPath.startsWith("/api/admin/") && "ADMIN".equals(roles.get(0));
        if (requestPath.startsWith("/api/admin/") && !adminRequest) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para administradores.");
            return;
        }

        try {
            transactionTemplate.executeWithoutResult(status -> {
                jdbcTemplate.queryForObject("SELECT set_config('app.teacher_id', ?, true)", String.class,
                        Long.toString(teacherId));
                if (adminRequest) {
                    jdbcTemplate.queryForObject("SELECT set_config('app.admin', 'true', true)", String.class);
                }
                try {
                    filterChain.doFilter(request, response);
                } catch (IOException | ServletException exception) {
                    status.setRollbackOnly();
                    throw new FilterExecutionException(exception);
                }
                // A failed request must not commit, and an inner @Transactional that threw has already
                // marked the transaction rollback-only: rolling back here avoids an UnexpectedRollbackException
                // that would write a second error body after the real one.
                if (response.getStatus() >= 400) status.setRollbackOnly();
            });
        } catch (FilterExecutionException exception) {
            if (exception.getCause() instanceof IOException ioException) throw ioException;
            if (exception.getCause() instanceof ServletException servletException) throw servletException;
            throw exception;
        }
    }

    private static final class FilterExecutionException extends RuntimeException {
        private FilterExecutionException(Exception cause) { super(cause); }
    }
}
