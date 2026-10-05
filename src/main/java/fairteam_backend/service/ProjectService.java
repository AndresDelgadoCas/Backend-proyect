package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.Project;
import fairteam_backend.repository.ProjectRepository;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    public Project save(Project project) {
        return projectRepository.save(project);
    }

    public Project update(Long id, Project project) {
        return projectRepository.findById(id)
                .map(existing -> {
                    existing.setName(project.getName());
                    existing.setDescription(project.getDescription());
                    return projectRepository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException("Project not found with id: " + id));
    }

    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new RuntimeException("Project not found with id: " + id);
        }

        projectRepository.deleteById(id);
    }
}