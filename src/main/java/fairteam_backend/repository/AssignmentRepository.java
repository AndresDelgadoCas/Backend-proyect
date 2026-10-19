
package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.evaluation.Assignment;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
}
