
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

import fairteam_backend.entity.evaluation.RubricCriterion;
import fairteam_backend.service.RubricCriterionService;

@RestController
@RequestMapping("/api/rubric-criteria")
@CrossOrigin(origins = "*")
public class RubricCriterionController {

    private final RubricCriterionService service;

    public RubricCriterionController(RubricCriterionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<RubricCriterion>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RubricCriterion> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/rubric/{rubricId}")
    public ResponseEntity<List<RubricCriterion>> findByRubricId(
            @PathVariable Long rubricId) {
        return ResponseEntity.ok(service.findByRubricId(rubricId));
    }

    @PostMapping
    public ResponseEntity<RubricCriterion> create(
            @RequestBody RubricCriterion criterion) {
        return ResponseEntity.ok(service.save(criterion));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RubricCriterion> update(
            @PathVariable Long id,
            @RequestBody RubricCriterion criterion) {
        return ResponseEntity.ok(service.update(id, criterion));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
