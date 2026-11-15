package fairteam_backend.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fairteam_backend.entity.TeacherUser;
import fairteam_backend.repository.TeacherUserRepository;

/**
 * Creates the first super administrator from ADMIN_EMAIL and ADMIN_PASSWORD (environment only, never in Git).
 * When the account already exists as a teacher it is promoted and its password is replaced by the configured
 * one, so nobody who registered that email first keeps access. After that the password is only replaced again
 * when ADMIN_PASSWORD_RESET=true.
 */
@Component
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger logger = LoggerFactory.getLogger(AdminBootstrap.class);
    static final int MIN_PASSWORD_LENGTH = 12;

    private final TeacherUserRepository users;
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final String email;
    private final String password;
    private final String name;
    private final boolean resetPassword;

    public AdminBootstrap(TeacherUserRepository users, JdbcTemplate jdbc,
            @Value("${fairteam.admin.email:}") String email,
            @Value("${fairteam.admin.password:}") String password,
            @Value("${fairteam.admin.name:Administrador FairTeam}") String name,
            @Value("${fairteam.admin.reset-password:false}") boolean resetPassword) {
        this.users = users;
        this.jdbc = jdbc;
        this.email = email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
        this.password = password == null ? "" : password;
        this.name = name;
        this.resetPassword = resetPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (email.isEmpty() && password.isEmpty()) {
            logger.info("Super administrator bootstrap skipped (ADMIN_EMAIL is not set).");
            return;
        }
        if (email.isEmpty() || !email.contains("@") || password.length() < MIN_PASSWORD_LENGTH) {
            logger.warn("Super administrator bootstrap skipped: ADMIN_EMAIL must be an email and ADMIN_PASSWORD at least {} characters.",
                    MIN_PASSWORD_LENGTH);
            return;
        }
        jdbc.execute("SELECT pg_advisory_xact_lock(14839201, 1)");
        TeacherUser user = users.findByEmail(email).orElse(null);
        if (user == null) {
            user = new TeacherUser();
            user.setEmail(email);
            user.setFullName(name);
            user.setPasswordHash(encoder.encode(password));
            user.setRole("ADMIN");
            user.setActive(true);
            users.save(user);
            logger.info("Super administrator account created for {}.", email);
            return;
        }
        boolean promoted = !"ADMIN".equals(user.getRole());
        if (promoted || resetPassword) user.setPasswordHash(encoder.encode(password));
        user.setRole("ADMIN");
        user.setActive(true);
        users.save(user);
        logger.info("Super administrator account ready for {} (promoted={}, passwordReplaced={}).",
                email, promoted, promoted || resetPassword);
    }
}
