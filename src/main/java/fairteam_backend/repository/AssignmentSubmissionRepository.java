
package fairteam_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.evaluation.AssignmentSubmission;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {

    List<AssignmentSubmission> findByAssignmentId(Long assignmentId);

    List<AssignmentSubmission> findByStudentId(Long studentId);
}
