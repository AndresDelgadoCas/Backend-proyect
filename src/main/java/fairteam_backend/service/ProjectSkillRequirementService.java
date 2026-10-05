package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.ProjectSkillRequirement;
import fairteam_backend.entity.ProjectSkillRequirementId;
import fairteam_backend.repository.ProjectSkillRequirementRepository;

@Service
public class ProjectSkillRequirementService {

    private final ProjectSkillRequirementRepository repository;

    public ProjectSkillRequirementService(ProjectSkillRequirementRepository repository) {
        this.repository = repository;
    }

    public List<ProjectSkillRequirement> findAll() {
        return repository.findAll();
    }

    public Optional<ProjectSkillRequirement> findById(ProjectSkillRequirementId id) {
        return repository.findById(id);
    }

    public ProjectSkillRequirement save(ProjectSkillRequirement requirement) {
        return repository.save(requirement);
    }

    public void delete(ProjectSkillRequirementId id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Project skill requirement not found");
        }

        repository.deleteById(id);
    }
}