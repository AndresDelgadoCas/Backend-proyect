
package fairteam_backend.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.entity.evaluation.AiEvaluation;
import fairteam_backend.entity.evaluation.AiEvaluation.EvaluationStatus;
import fairteam_backend.service.AiEvaluationService;

@RestController
@RequestMapping("/api/ai-evaluations")
@CrossOrigin(origins = "*")
public class AiEvaluationController {

    private final AiEvaluationService service;

    public AiEvaluationController(AiEvaluationService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AiEvaluation>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiEvaluation> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/submission/{submissionId}")
    public ResponseEntity<AiEvaluation> findBySubmissionId(
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(
                service.findBySubmissionId(submissionId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<AiEvaluation>> findByStatus(
            @PathVariable EvaluationStatus status) {
        return ResponseEntity.ok(service.findByStatus(status));
    }

    @PostMapping("/submission/{submissionId}")
    public ResponseEntity<AiEvaluation> createPendingEvaluation(
            @PathVariable Long submissionId) {
        return ResponseEntity.ok(
                service.createPendingEvaluation(submissionId));
    }

    @PutMapping("/{id}/propose")
    public ResponseEntity<AiEvaluation> saveProposedResult(
            @PathVariable Long id,
            @RequestBody ProposedResultRequest request) {
        return ResponseEntity.ok(
                service.saveProposedResult(
                        id,
                        request.suggestedGrade(),
                        request.feedback(),
                        request.criteriaResults()));
    }

    @PutMapping("/{id}/review")
    public ResponseEntity<AiEvaluation> reviewEvaluation(
            @PathVariable Long id,
            @RequestParam Long reviewerId,
            @RequestParam boolean approved) {
        return ResponseEntity.ok(
                service.reviewEvaluation(id, reviewerId, approved));
    }

    public record ProposedResultRequest(
            BigDecimal suggestedGrade,
            String feedback,
            String criteriaResults) {
    }
}
