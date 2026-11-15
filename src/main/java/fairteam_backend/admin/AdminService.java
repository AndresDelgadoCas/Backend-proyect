package fairteam_backend.admin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.entity.TeacherUser;
import fairteam_backend.repository.TeacherUserRepository;

/** Super administrator operations. Reads span accounts (read-only RLS policy); writes only touch accounts and the audit log. */
@Service
public class AdminService {
    static final Set<String> ROLES = Set.of("TEACHER", "ADMIN");
    private static final String PASSWORD_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final TeacherUserRepository users;
    private final JdbcTemplate jdbc;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();
    private final String aiBaseUrl;

    public AdminService(TeacherUserRepository users, JdbcTemplate jdbc,
            @Value("${fairteam.ai.base-url:http://localhost:8000}") String aiBaseUrl) {
        this.users = users;
        this.jdbc = jdbc;
        this.aiBaseUrl = aiBaseUrl;
    }

    public record Overview(long accounts, long activeAccounts, long admins, long signedInLast7Days,
            long newLast7Days, long newLast30Days, long courses, long students, long grades,
            long materials, long workshops, List<Map<String, Object>> signupsByDay) { }

    public record AccountRow(long id, String name, String email, String role, boolean active,
            LocalDateTime createdAt, LocalDateTime lastLoginAt, long courses, long students,
            long grades, long materials) { }

    public record CourseRow(long id, String name, String period, LocalDateTime createdAt,
            long students, long grades, long materials) { }

    public record AccountDetail(AccountRow account, List<CourseRow> courses) { }

    public record AuditRow(long id, String admin, String action, Long targetId, String target,
            String detail, LocalDateTime createdAt) { }

    public record PasswordReset(String temporaryPassword) { }

    @Transactional(readOnly = true)
    public Overview overview() {
        Map<String, Object> a = jdbc.queryForMap("""
                SELECT count(*) AS accounts,
                       count(*) FILTER (WHERE active) AS active_accounts,
                       count(*) FILTER (WHERE role = 'ADMIN') AS admins,
                       count(*) FILTER (WHERE last_login_at > now() - interval '7 days') AS signed_in,
                       count(*) FILTER (WHERE created_at > now() - interval '7 days') AS new7,
                       count(*) FILTER (WHERE created_at > now() - interval '30 days') AS new30
                FROM teacher_users""");
        Map<String, Object> t = jdbc.queryForMap("""
                SELECT (SELECT count(*) FROM courses) AS courses,
                       (SELECT count(*) FROM students) AS students,
                       (SELECT count(*) FROM grades) AS grades,
                       (SELECT count(*) FROM course_materials) AS materials,
                       (SELECT count(*) FROM workshops) AS workshops""");
        List<Map<String, Object>> signups = jdbc.query("""
                SELECT d::date AS day, count(u.id) AS signups
                FROM generate_series(current_date - 13, current_date, interval '1 day') d
                LEFT JOIN teacher_users u ON u.created_at::date = d::date
                GROUP BY d ORDER BY d""",
                (rs, i) -> Map.<String, Object>of("day", rs.getDate("day").toString(), "signups", rs.getLong("signups")));
        return new Overview(n(a, "accounts"), n(a, "active_accounts"), n(a, "admins"), n(a, "signed_in"),
                n(a, "new7"), n(a, "new30"), n(t, "courses"), n(t, "students"), n(t, "grades"),
                n(t, "materials"), n(t, "workshops"), signups);
    }

    @Transactional(readOnly = true)
    public List<AccountRow> accounts() {
        return jdbc.query(ACCOUNT_SQL + " ORDER BY u.created_at DESC, u.id DESC LIMIT 2000", this::account);
    }

    @Transactional(readOnly = true)
    public AccountDetail detail(long id) {
        AccountRow account = jdbc.query(ACCOUNT_SQL + " WHERE u.id = ?", this::account, id).stream().findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada."));
        List<CourseRow> courses = jdbc.query("""
                SELECT c.id, c.name, c.period, c.created_at,
                       (SELECT count(*) FROM students s WHERE s.course_id = c.id) AS students,
                       (SELECT count(*) FROM grades g WHERE g.course_id = c.id) AS grades,
                       (SELECT count(*) FROM course_materials m WHERE m.course_id = c.id) AS materials
                FROM courses c WHERE c.teacher_id = ? ORDER BY c.created_at DESC""",
                (rs, i) -> new CourseRow(rs.getLong("id"), rs.getString("name"), rs.getString("period"),
                        time(rs.getTimestamp("created_at")), rs.getLong("students"), rs.getLong("grades"),
                        rs.getLong("materials")), id);
        return new AccountDetail(account, courses);
    }

    /** Activates/deactivates or changes the role of an account, never leaving the platform without an active admin. */
    @Transactional
    public AccountRow update(long adminId, long targetId, Boolean active, String role) {
        TeacherUser target = users.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada."));
        if (active == null && role == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay cambios que aplicar.");
        }
        if (role != null && !ROLES.contains(role)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol no válido.");
        }
        boolean losesAdmin = "ADMIN".equals(target.getRole())
                && ((active != null && !active) || (role != null && !role.equals("ADMIN")));
        if (losesAdmin) {
            if (targetId == adminId) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No puedes desactivar ni quitarte el rol de administrador a ti mismo.");
            }
            if (users.countByRoleAndActive("ADMIN", true) <= 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Debe quedar al menos un administrador activo.");
            }
        }
        if (active != null && !active.equals(target.getActive())) {
            target.setActive(active);
            audit(adminId, active ? "ACCOUNT_ACTIVATED" : "ACCOUNT_DEACTIVATED", targetId, target.getEmail());
        }
        if (role != null && !role.equals(target.getRole())) {
            audit(adminId, "ROLE_CHANGED", targetId, target.getRole() + " -> " + role);
            target.setRole(role);
        }
        users.saveAndFlush(target);
        return jdbc.query(ACCOUNT_SQL + " WHERE u.id = ?", this::account, targetId).get(0);
    }

    /** Sets a random temporary password, shown once to the administrator. Other administrators cannot be reset. */
    @Transactional
    public PasswordReset resetPassword(long adminId, long targetId) {
        TeacherUser target = users.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada."));
        if ("ADMIN".equals(target.getRole())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "La contraseña de un administrador no se restablece desde el panel.");
        }
        StringBuilder password = new StringBuilder();
        for (int i = 0; i < 16; i++) password.append(PASSWORD_ALPHABET.charAt(random.nextInt(PASSWORD_ALPHABET.length())));
        target.setPasswordHash(encoder.encode(password));
        users.save(target);
        audit(adminId, "PASSWORD_RESET", targetId, target.getEmail());
        return new PasswordReset(password.toString());
    }

    @Transactional(readOnly = true)
    public List<AuditRow> audit(int limit) {
        int capped = Math.max(1, Math.min(limit, 200));
        return jdbc.query("""
                SELECT l.id, a.email AS admin, l.action, l.target_id, t.email AS target, l.detail, l.created_at
                FROM admin_audit_log l
                JOIN teacher_users a ON a.id = l.admin_id
                LEFT JOIN teacher_users t ON t.id = l.target_id
                ORDER BY l.created_at DESC, l.id DESC LIMIT ?""",
                (rs, i) -> new AuditRow(rs.getLong("id"), rs.getString("admin"), rs.getString("action"),
                        (Long) rs.getObject("target_id"), rs.getString("target"), rs.getString("detail"),
                        time(rs.getTimestamp("created_at"))), capped);
    }

    public Map<String, Object> system() {
        boolean database;
        try {
            database = Integer.valueOf(1).equals(jdbc.queryForObject("SELECT 1", Integer.class));
        } catch (RuntimeException exception) {
            database = false;
        }
        boolean reachable = false;
        String model = null;
        try {
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
            HttpResponse<String> response = client.send(
                    HttpRequest.newBuilder(URI.create(aiBaseUrl.replaceAll("/+$", "") + "/health"))
                            .timeout(Duration.ofSeconds(4)).GET().build(),
                    HttpResponse.BodyHandlers.ofString());
            reachable = response.statusCode() == 200;
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"model\"\\s*:\\s*\"([^\"]+)\"").matcher(response.body());
            if (reachable && m.find()) model = m.group(1);
        } catch (java.io.IOException | IllegalArgumentException exception) {
            reachable = false;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
        Map<String, Object> ai = new java.util.LinkedHashMap<>();
        ai.put("reachable", reachable);
        ai.put("model", model);
        return Map.of("database", database, "ai", ai);
    }

    void audit(long adminId, String action, Long targetId, String detail) {
        jdbc.update("INSERT INTO admin_audit_log(admin_id, action, target_id, detail) VALUES (?, ?, ?, ?)",
                adminId, action, targetId, detail == null ? null : detail.substring(0, Math.min(detail.length(), 500)));
    }

    private static final String ACCOUNT_SQL = """
            SELECT u.id, u.full_name, u.email, u.role, u.active, u.created_at, u.last_login_at,
                   COALESCE(c.n, 0) AS courses, COALESCE(s.n, 0) AS students,
                   COALESCE(g.n, 0) AS grades, COALESCE(m.n, 0) AS materials
            FROM teacher_users u
            LEFT JOIN (SELECT teacher_id, count(*) n FROM courses GROUP BY teacher_id) c ON c.teacher_id = u.id
            LEFT JOIN (SELECT teacher_id, count(*) n FROM students GROUP BY teacher_id) s ON s.teacher_id = u.id
            LEFT JOIN (SELECT teacher_id, count(*) n FROM grades GROUP BY teacher_id) g ON g.teacher_id = u.id
            LEFT JOIN (SELECT teacher_id, count(*) n FROM course_materials GROUP BY teacher_id) m ON m.teacher_id = u.id""";

    private AccountRow account(java.sql.ResultSet rs, int row) throws java.sql.SQLException {
        return new AccountRow(rs.getLong("id"), rs.getString("full_name"), rs.getString("email"),
                rs.getString("role"), rs.getBoolean("active"), time(rs.getTimestamp("created_at")),
                time(rs.getTimestamp("last_login_at")), rs.getLong("courses"), rs.getLong("students"),
                rs.getLong("grades"), rs.getLong("materials"));
    }

    private static LocalDateTime time(Timestamp value) { return value == null ? null : value.toLocalDateTime(); }

    private static long n(Map<String, Object> row, String key) { return ((Number) row.get(key)).longValue(); }
}
