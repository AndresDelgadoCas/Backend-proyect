package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> {
}