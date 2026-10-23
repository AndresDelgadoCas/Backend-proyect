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

        try {
            transactionTemplate.executeWithoutResult(status -> {
                jdbcTemplate.queryForObject("SELECT set_config('app.teacher_id', ?, true)", String.class,
                        Long.toString(teacherId));
                try {
                    filterChain.doFilter(request, response);
                } catch (IOException | ServletException exception) {
                    status.setRollbackOnly();
                    throw new FilterExecutionException(exception);
                }
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
