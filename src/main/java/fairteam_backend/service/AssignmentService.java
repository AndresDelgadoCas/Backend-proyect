
package fairteam_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.evaluation.Assignment;
import fairteam_backend.repository.AssignmentRepository;
import fairteam_backend.repository.SubjectRepository;
import fairteam_backend.repository.TeacherUserRepository;

@Service
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherUserRepository teacherUserRepository;

    public AssignmentService(
            AssignmentRepository assignmentRepository,
            SubjectRepository subjectRepository,
            TeacherUserRepository teacherUserRepository) {
        this.assignmentRepository = assignmentRepository;
        this.subjectRepository = subjectRepository;
        this.teacherUserRepository = teacherUserRepository;
    }

    public List<Assignment> findAll() {
        return assignmentRepository.findAll();
    }

    public Assignment findById(Long id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Assignment not found: " + id));
    }

    public Assignment save(Assignment assignment) {
        validateAssignment(assignment);
        return assignmentRepository.save(assignment);
    }

    public Assignment update(Long id, Assignment assignment) {
        Assignment existing = findById(id);
        validateAssignment(assignment);

        existing.setTitle(assignment.getTitle());
        existing.setDescription(assignment.getDescription());
        existing.setInstructions(assignment.getInstructions());
        existing.setDueDate(assignment.getDueDate());
        existing.setMaxScore(assignment.getMaxScore());
        existing.setSubject(assignment.getSubject());
        existing.setTeacher(assignment.getTeacher());

        return assignmentRepository.save(existing);
    }

    public void delete(Long id) {
        if (!assignmentRepository.existsById(id)) {
            throw new RuntimeException("Assignment not found: " + id);
        }

        assignmentRepository.deleteById(id);
    }

    private void validateAssignment(Assignment assignment) {
        if (assignment == null) {
            throw new IllegalArgumentException("Assignment cannot be null");
        }

        if (assignment.getTitle() == null
                || assignment.getTitle().isBlank()) {
            throw new IllegalArgumentException("Title is required");
        }

        if (assignment.getSubject() == null
                || assignment.getSubject().getId() == null
                || !subjectRepository.existsById(
                        assignment.getSubject().getId())) {
            throw new IllegalArgumentException("A valid subject is required");
        }

        if (assignment.getTeacher() == null
                || assignment.getTeacher().getId() == null
                || !teacherUserRepository.existsById(
                        assignment.getTeacher().getId())) {
            throw new IllegalArgumentException("A valid teacher is required");
        }

        if (assignment.getMaxScore() == null
                || assignment.getMaxScore().signum() <= 0) {
            throw new IllegalArgumentException(
                    "Maximum score must be greater than zero");
        }
    }
}
