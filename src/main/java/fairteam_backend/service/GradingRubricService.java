
package fairteam_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.evaluation.GradingRubric;
import fairteam_backend.repository.GradingRubricRepository;

@Service
public class GradingRubricService {

    private final GradingRubricRepository rubricRepository;

    public GradingRubricService(
            GradingRubricRepository rubricRepository) {
        this.rubricRepository = rubricRepository;
    }

    public List<GradingRubric> findAll() {
        return rubricRepository.findAll();
    }

    public GradingRubric findById(Long id) {
        return rubricRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Rubric not found: " + id));
    }

    public GradingRubric findByAssignmentId(Long assignmentId) {
        return rubricRepository.findByAssignmentId(assignmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Rubric not found for assignment: "
                                        + assignmentId));
    }

    public GradingRubric save(GradingRubric rubric) {
        validateRubric(rubric);
        return rubricRepository.save(rubric);
    }

    public GradingRubric update(Long id, GradingRubric rubric) {
        GradingRubric existing = findById(id);
        validateRubric(rubric);

        existing.setName(rubric.getName());
        existing.setDescription(rubric.getDescription());
        existing.setAssignment(rubric.getAssignment());

        return rubricRepository.save(existing);
    }

    public void delete(Long id) {
        if (!rubricRepository.existsById(id)) {
            throw new RuntimeException("Rubric not found: " + id);
        }

        rubricRepository.deleteById(id);
    }

    private void validateRubric(GradingRubric rubric) {
        if (rubric == null) {
            throw new IllegalArgumentException(
                    "Rubric cannot be null");
        }

        if (rubric.getName() == null
                || rubric.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Rubric name is required");
        }

        if (rubric.getAssignment() == null
                || rubric.getAssignment().getId() == null) {
            throw new IllegalArgumentException(
                    "A valid assignment is required");
        }
    }
}
