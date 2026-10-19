
package fairteam_backend.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import fairteam_backend.entity.evaluation.AiEvaluation;
import fairteam_backend.entity.evaluation.AiEvaluation.EvaluationStatus;
import fairteam_backend.entity.evaluation.AssignmentSubmission;
import fairteam_backend.repository.AiEvaluationRepository;
import fairteam_backend.repository.AssignmentSubmissionRepository;

@Service
@Transactional
public class AiEvaluationService {

    private final AiEvaluationRepository evaluationRepository;
    private final AssignmentSubmissionRepository submissionRepository;

    public AiEvaluationService(
            AiEvaluationRepository evaluationRepository,
            AssignmentSubmissionRepository submissionRepository) {
        this.evaluationRepository = evaluationRepository;
        this.submissionRepository = submissionRepository;
    }

    @Transactional(readOnly = true)
    public List<AiEvaluation> findAll() {
        return evaluationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public AiEvaluation findById(Long id) {
        return evaluationRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evaluation not found: " + id));
    }

    @Transactional(readOnly = true)
    public AiEvaluation findBySubmissionId(Long submissionId) {
        return evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Evaluation not found for submission: "
                                        + submissionId));
    }

    public AiEvaluation createPendingEvaluation(Long submissionId) {
        AssignmentSubmission submission =
                submissionRepository.findById(submissionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Submission not found: "
                                                + submissionId));

        if (evaluationRepository.findBySubmissionId(submissionId)
                .isPresent()) {
            throw new IllegalArgumentException(
                    "An evaluation already exists for this submission.");
        }

        AiEvaluation evaluation = new AiEvaluation();
        evaluation.setSubmission(submission);
        evaluation.setStatus(EvaluationStatus.PENDING);
        evaluation.setCriteriaResults("{}");

        return evaluationRepository.save(evaluation);
    }

    public AiEvaluation saveProposedResult(
            Long evaluationId,
            BigDecimal suggestedGrade,
            String feedback,
            String criteriaResults) {

        AiEvaluation evaluation = findById(evaluationId);

        if (suggestedGrade == null
                || suggestedGrade.signum() < 0
                || suggestedGrade.compareTo(new BigDecimal("999.99")) > 0) {
            throw new IllegalArgumentException(
                    "Suggested grade must be between 0 and 999.99.");
        }

        evaluation.setSuggestedGrade(suggestedGrade);
        evaluation.setFeedback(feedback);
        evaluation.setCriteriaResults(
                criteriaResults == null || criteriaResults.isBlank()
                        ? "{}" : criteriaResults);
        evaluation.setStatus(EvaluationStatus.PROPOSED);

        return evaluationRepository.save(evaluation);
    }

    public AiEvaluation reviewEvaluation(
            Long evaluationId,
            Long reviewerId,
            boolean approved) {

        AiEvaluation evaluation = findById(evaluationId);

        if (evaluation.getStatus() != EvaluationStatus.PROPOSED) {
            throw new IllegalStateException(
                    "Only proposed evaluations can be reviewed.");
        }

        if (reviewerId == null) {
            throw new IllegalArgumentException(
                    "Reviewer ID is required.");
        }

        evaluation.setReviewedBy(reviewerId);
        evaluation.setReviewedAt(LocalDateTime.now());
        evaluation.setStatus(
                approved
                        ? EvaluationStatus.APPROVED
                        : EvaluationStatus.REJECTED);

        return evaluationRepository.save(evaluation);
    }

    @Transactional(readOnly = true)
    public List<AiEvaluation> findByStatus(EvaluationStatus status) {
        if (status == null) {
            throw new IllegalArgumentException(
                    "Evaluation status is required.");
        }

        return evaluationRepository.findByStatus(status);
    }
}
