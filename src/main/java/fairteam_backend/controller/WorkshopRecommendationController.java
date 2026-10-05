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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.entity.WorkshopRecommendation;
import fairteam_backend.factory.Recommendation;
import fairteam_backend.service.WorkshopRecommendationService;

@RestController
@RequestMapping("/api/workshop-recommendations")
@CrossOrigin(origins = "*")
public class WorkshopRecommendationController {

    private final WorkshopRecommendationService service;

    public WorkshopRecommendationController(
            WorkshopRecommendationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<WorkshopRecommendation>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkshopRecommendation> findById(
            @PathVariable Long id) {

        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<WorkshopRecommendation> create(
            @RequestBody WorkshopRecommendation recommendation) {

        return ResponseEntity.ok(
                service.save(recommendation)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkshopRecommendation> update(
            @PathVariable Long id,
            @RequestBody WorkshopRecommendation recommendation) {

        return ResponseEntity.ok(
                service.update(id, recommendation)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/factory")
    public ResponseEntity<Recommendation> createFactoryRecommendation(
            @RequestParam String message) {

        return ResponseEntity.ok(
                service.createWorkshopRecommendation(message)
        );
    }
}