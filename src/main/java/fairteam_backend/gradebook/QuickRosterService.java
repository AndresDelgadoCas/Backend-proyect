package fairteam_backend.gradebook;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Adds several students at once from pasted lines: "Ana Ruiz", "Ana Ruiz; ana@u.edu" or
 * "Ana Ruiz, ana@u.edu" (also tab-separated, as copied from Excel). Repeated names or emails are skipped
 * and reported instead of failing the whole paste.
 */
@Service
public class QuickRosterService {
    public static final int MAX_LINES = 200;
    private static final Pattern EMAIL = Pattern.compile("[^\\s@]+@[^\\s@]+\\.[^\\s@]+");
    private static final Pattern SEPARATOR = Pattern.compile("\\s*[;,\\t]\\s*");

    public record Entry(String name, String email) { }

    public record Result(List<String> created, List<String> skipped) { }

    private final JdbcTemplate jdbc;

    public QuickRosterService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    /** Parses pasted text into entries; pure, so it is tested without a database. */
    public static List<Entry> parse(String text) {
        List<Entry> entries = new ArrayList<>();
        if (text == null) return entries;
        for (String line : text.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            String name = trimmed;
            String email = null;
            String[] parts = SEPARATOR.split(trimmed, 2);
            if (parts.length == 2 && EMAIL.matcher(parts[1].trim()).matches()) {
                name = parts[0].trim();
                email = parts[1].trim().toLowerCase(Locale.ROOT);
            } else if (EMAIL.matcher(trimmed).matches()) {
                continue; // a bare email is not a name
            }
            name = name.replaceAll("\\s+", " ").replaceAll("^\\d+[.)-]\\s*", "");
            if (!name.isBlank()) entries.add(new Entry(name, email));
        }
        return entries;
    }

    @Transactional
    public Result add(long courseId, String text) {
        Integer exists = jdbc.queryForObject("SELECT count(*) FROM courses WHERE id = ?", Integer.class, courseId);
        if (exists == null || exists == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
        List<Entry> entries = parse(text);
        if (entries.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe al menos un nombre.");
        if (entries.size() > MAX_LINES) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pega máximo " + MAX_LINES + " estudiantes a la vez.");

        Set<String> names = new HashSet<>(jdbc.queryForList("SELECT lower(name) FROM students WHERE course_id = ?", String.class, courseId));
        Set<String> emails = new HashSet<>(jdbc.queryForList("SELECT lower(email) FROM students WHERE email IS NOT NULL", String.class));
        List<String> created = new ArrayList<>();
        List<String> skipped = new ArrayList<>();
        for (Entry entry : entries) {
            String key = entry.name().toLowerCase(Locale.ROOT);
            if (entry.name().length() > 150) { skipped.add(entry.name().substring(0, 40) + "… (nombre muy largo)"); continue; }
            if (names.contains(key)) { skipped.add(entry.name() + " (ya está en el curso)"); continue; }
            if (entry.email() != null && emails.contains(entry.email())) { skipped.add(entry.name() + " (correo repetido)"); continue; }
            jdbc.update("INSERT INTO students (name, email, academic_level, course_id) VALUES (?, ?, 'MEDIUM', ?)",
                    entry.name(), entry.email(), courseId);
            names.add(key);
            if (entry.email() != null) emails.add(entry.email());
            created.add(entry.name());
        }
        return new Result(created, skipped);
    }
}
