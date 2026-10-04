package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.ProjectSkillRequirement;
import fairteam_backend.entity.ProjectSkillRequirementId;

public interface ProjectSkillRequirementRepository
        extends JpaRepository<ProjectSkillRequirement, ProjectSkillRequirementId> {
}