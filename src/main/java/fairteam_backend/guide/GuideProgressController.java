package fairteam_backend.guide;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.databind.JsonNode;

/** The signed-in teacher's progress in "Primeros pasos": 204 means the account has never opened it. */
@RestController
public class GuideProgressController {
    private final GuideProgressService guide;

    public GuideProgressController(GuideProgressService guide) { this.guide = guide; }

    @GetMapping("/api/me/guide")
    public ResponseEntity<JsonNode> get() {
        return guide.get().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping("/api/me/guide")
    public JsonNode save(@RequestBody JsonNode body) {
        return guide.save(body);
    }
}
