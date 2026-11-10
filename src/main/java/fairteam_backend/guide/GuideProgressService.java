package fairteam_backend.guide;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Where each teacher is in "Primeros pasos". Stored per account (row-level security keeps it to its owner), so
 * the guide does not start over on another computer and appears once for every new account.
 */
@Service
public class GuideProgressService {
    private static final Set<String> STEPS = Set.of("perfil", "curso", "estudiantes", "columnas", "taller", "rubrica", "calificar", "libro", "buho");
    private static final JsonMapper JSON = JsonMapper.builder().build();
    private final JdbcTemplate jdbc;

    public GuideProgressService(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional(readOnly = true)
    public Optional<JsonNode> get() {
        List<String> rows = jdbc.queryForList("SELECT state::text FROM teacher_guide_progress", String.class);
        return rows.stream().findFirst().map(JSON::readTree);
    }

    @Transactional
    public JsonNode save(JsonNode body) {
        JsonNode clean = clean(body);
        jdbc.update("""
                INSERT INTO teacher_guide_progress (state) VALUES (?::jsonb)
                ON CONFLICT (teacher_id) DO UPDATE SET state = EXCLUDED.state, updated_at = now()""", JSON.writeValueAsString(clean));
        return clean;
    }

    /** Only the known fields and step ids are kept: the browser cannot store anything else here. */
    static JsonNode clean(JsonNode body) {
        if (body == null || !body.isObject()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El avance de la guía no es válido.");
        ObjectNode out = JSON.createObjectNode();
        for (String flag : List.of("open", "minimized", "finished")) out.put(flag, body.path(flag).asBoolean(false));
        out.set("done", steps(body.path("done")));
        out.set("skipped", steps(body.path("skipped")));
        return out;
    }

    private static ArrayNode steps(JsonNode list) {
        ArrayNode out = JSON.createArrayNode();
        if (list.isArray()) for (JsonNode item : list) {
            String id = item.asString("");
            if (STEPS.contains(id) && !contains(out, id)) out.add(id);
        }
        return out;
    }

    private static boolean contains(ArrayNode list, String id) {
        for (JsonNode item : list) if (id.equals(item.asString(""))) return true;
        return false;
    }
}
