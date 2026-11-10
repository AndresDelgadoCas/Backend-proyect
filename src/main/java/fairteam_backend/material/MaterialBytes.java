package fairteam_backend.material;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Reads the bytes of a material straight from JDBC.
 *
 * <p>A Spring Data query returning {@code byte[]} goes through a conversion that boxes every byte into a
 * {@code Byte} object: a 9 MB textbook became ~150 MB of heap and the service ran out of memory. A plain
 * {@code getBytes} keeps it at the size of the file.
 */
@Component
public class MaterialBytes {
    private final JdbcTemplate jdbc;

    public MaterialBytes(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<byte[]> load(long id, long courseId) {
        List<byte[]> rows = jdbc.query("SELECT content FROM course_materials WHERE id = ? AND course_id = ?",
                (rs, i) -> rs.getBytes(1), id, courseId);
        return rows.stream().findFirst();
    }
}
