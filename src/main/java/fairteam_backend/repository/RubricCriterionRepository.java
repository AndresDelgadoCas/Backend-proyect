
package fairteam_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.evaluation.RubricCriterion;

public interface RubricCriterionRepository extends JpaRepository<RubricCriterion, Long> {

    List<RubricCriterion> findByRubricId(Long rubricId);
}
