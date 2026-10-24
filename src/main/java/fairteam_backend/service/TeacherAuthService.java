package fairteam_backend.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.entity.TeacherUser;
import fairteam_backend.repository.TeacherUserRepository;

@Service
public class TeacherAuthService {
    private final TeacherUserRepository users;
    private final JwtEncoder jwtEncoder;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JdbcTemplate jdbcTemplate;

    public TeacherAuthService(TeacherUserRepository users, JwtEncoder jwtEncoder, JdbcTemplate jdbcTemplate) {
        this.users = users;
        this.jwtEncoder = jwtEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public AuthResult register(String name, String email, String password) {
        // Serialize registration without relying on a migration-created table.
        jdbcTemplate.execute("SELECT pg_advisory_xact_lock(14839201, 1)");
        String normalizedEmail = normalizeEmail(email);
        if (users.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese correo.");
        }
        boolean firstAccount = users.count() == 0;
        TeacherUser user = new TeacherUser();
        user.setFullName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("TEACHER");
        user.setActive(true);
        TeacherUser saved = users.save(user);
        if (firstAccount) {
            claimUnownedData(saved.getId());
        }
        return issue(saved);
    }

    @Transactional(readOnly = true)
    public AuthResult login(String email, String password) {
        TeacherUser user = users.findByEmail(normalizeEmail(email))
                .filter(TeacherUser::getActive)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos."));
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }
        return issue(user);
    }

    private AuthResult issue(TeacherUser user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("fairteam")
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(8, ChronoUnit.HOURS))
                .claim("email", user.getEmail())
                .claim("name", user.getFullName())
                .claim("roles", List.of("TEACHER"))
                .build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
        return new AuthResult(token, user.getId(), user.getFullName(), user.getEmail(), "TEACHER");
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
    }

    private void claimUnownedData(Long teacherId) {
        jdbcTemplate.queryForObject("SELECT set_config('app.teacher_id', ?, true)", String.class, teacherId.toString());
        jdbcTemplate.queryForObject("SELECT set_config('app.bootstrap', 'true', true)", String.class);
        List<String> tenantTables = List.of(
                "grades", "project_skill_requirements", "projects", "skills",
                "student_availability", "student_project_preferences", "student_skills",
                "students", "subjects", "team_students", "teams",
                "workshop_recommendations", "workshops", "assignments",
                "assignment_submissions", "grading_rubrics", "rubric_criteria", "ai_evaluations");
        for (String table : tenantTables) {
            jdbcTemplate.update("UPDATE " + table + " SET teacher_id = ? WHERE teacher_id IS NULL", teacherId);
        }
        jdbcTemplate.queryForObject("SELECT set_config('app.bootstrap', 'false', true)", String.class);
    }

    public record AuthResult(String accessToken, Long id, String name, String email, String role) { }
}
