package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.TeamStudent;
import fairteam_backend.entity.TeamStudentId;

public interface TeamStudentRepository
        extends JpaRepository<TeamStudent, TeamStudentId> {
}