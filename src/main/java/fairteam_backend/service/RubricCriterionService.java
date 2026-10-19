
package fairteam_backend.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.evaluation.RubricCriterion;
import fairteam_backend.repository.RubricCriterionRepository;

@Service
public class RubricCriterionService {

    private final RubricCriterionRepository criterionRepository;

    public RubricCriterionService(
            RubricCriterionRepository criterionRepository) {
        this.criterionRepository = criterionRepository;
    }

    public List<RubricCriterion> findAll() {
        return criterionRepository.findAll();
    }

    public RubricCriterion findById(Long id) {
        return criterionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Criterion not found: " + id));
    }

    public List<RubricCriterion> findByRubricId(Long rubricId) {
        return criterionRepository.findByRubricId(rubricId);
    }

    public RubricCriterion save(RubricCriterion criterion) {
        validateCriterion(criterion);
        return criterionRepository.save(criterion);
    }

    public RubricCriterion update(Long id, RubricCriterion criterion) {
        RubricCriterion existing = findById(id);
        validateCriterion(criterion);

        existing.setName(criterion.getName());
        existing.setDescription(criterion.getDescription());
        existing.setMaxScore(criterion.getMaxScore());
        existing.setWeight(criterion.getWeight());
        existing.setRubric(criterion.getRubric());

        return criterionRepository.save(existing);
    }

    public void delete(Long id) {
        if (!criterionRepository.existsById(id)) {
            throw new RuntimeException("Criterion not found: " + id);
        }

        criterionRepository.deleteById(id);
    }

    private void validateCriterion(RubricCriterion criterion) {
        if (criterion == null) {
            throw new IllegalArgumentException(
                    "Criterion cannot be null");
        }

        if (criterion.getName() == null
                || criterion.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Criterion name is required");
        }

        if (criterion.getRubric() == null
                || criterion.getRubric().getId() == null) {
            throw new IllegalArgumentException(
                    "A valid rubric is required");
        }

        if (criterion.getMaxScore() == null
                || criterion.getMaxScore().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Maximum score must be greater than zero");
        }

        if (criterion.getWeight() == null
                || criterion.getWeight().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Weight must be greater than zero");
        }
    }
}
