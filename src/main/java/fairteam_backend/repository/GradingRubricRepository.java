
package fairteam_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.evaluation.GradingRubric;

public interface GradingRubricRepository extends JpaRepository<GradingRubric, Long> {

    Optional<GradingRubric> findByAssignmentId(Long assignmentId);
}
