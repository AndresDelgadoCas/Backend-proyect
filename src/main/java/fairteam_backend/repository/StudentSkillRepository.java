package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.StudentSkill;
import fairteam_backend.entity.StudentSkillId;

public interface StudentSkillRepository extends JpaRepository<StudentSkill, StudentSkillId> {
}