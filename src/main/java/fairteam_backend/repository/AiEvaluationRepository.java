package fairteam_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.evaluation.AiEvaluation;
import fairteam_backend.entity.evaluation.AiEvaluation.EvaluationStatus;

public interface AiEvaluationRepository extends JpaRepository<AiEvaluation, Long> {

    Optional<AiEvaluation> findBySubmissionId(Long submissionId);

    List<AiEvaluation> findByStatus(EvaluationStatus status);
}