
package fairteam_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.evaluation.AssignmentSubmission;
import fairteam_backend.repository.AssignmentSubmissionRepository;

@Service
public class AssignmentSubmissionService {

    private final AssignmentSubmissionRepository submissionRepository;

    public AssignmentSubmissionService(
            AssignmentSubmissionRepository submissionRepository) {
        this.submissionRepository = submissionRepository;
    }

    public List<AssignmentSubmission> findAll() {
        return submissionRepository.findAll();
    }

    public AssignmentSubmission findById(Long id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Submission not found: " + id));
    }

    public List<AssignmentSubmission> findByAssignmentId(Long assignmentId) {
        return submissionRepository.findByAssignmentId(assignmentId);
    }

    public List<AssignmentSubmission> findByStudentId(Long studentId) {
        return submissionRepository.findByStudentId(studentId);
    }

    public AssignmentSubmission save(AssignmentSubmission submission) {
        if (submission == null) {
            throw new IllegalArgumentException(
                    "Submission cannot be null");
        }

        if (submission.getAssignment() == null
                || submission.getAssignment().getId() == null) {
            throw new IllegalArgumentException(
                    "A valid assignment is required");
        }

        if (submission.getStudent() == null
                || submission.getStudent().getId() == null) {
            throw new IllegalArgumentException(
                    "A valid student is required");
        }

        if ((submission.getSubmissionText() == null
                || submission.getSubmissionText().isBlank())
                && (submission.getFileUrl() == null
                || submission.getFileUrl().isBlank())) {
            throw new IllegalArgumentException(
                    "Submission text or a file URL is required");
        }

        return submissionRepository.save(submission);
    }

    public void delete(Long id) {
        if (!submissionRepository.existsById(id)) {
            throw new RuntimeException("Submission not found: " + id);
        }

        submissionRepository.deleteById(id);
    }
}
