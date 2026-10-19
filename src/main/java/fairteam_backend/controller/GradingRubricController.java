
package fairteam_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.entity.evaluation.GradingRubric;
import fairteam_backend.service.GradingRubricService;

@RestController
@RequestMapping("/api/grading-rubrics")
@CrossOrigin(origins = "*")
public class GradingRubricController {

    private final GradingRubricService service;

    public GradingRubricController(GradingRubricService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<GradingRubric>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GradingRubric> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<GradingRubric> findByAssignmentId(
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(
                service.findByAssignmentId(assignmentId));
    }

    @PostMapping
    public ResponseEntity<GradingRubric> create(
            @RequestBody GradingRubric rubric) {
        return ResponseEntity.ok(service.save(rubric));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GradingRubric> update(
            @PathVariable Long id,
            @RequestBody GradingRubric rubric) {
        return ResponseEntity.ok(service.update(id, rubric));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
