package fairteam_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.entity.ProjectSkillRequirement;
import fairteam_backend.entity.ProjectSkillRequirementId;
import fairteam_backend.service.ProjectSkillRequirementService;

@RestController
@RequestMapping("/api/project-skill-requirements")
@CrossOrigin(origins = "*")
public class ProjectSkillRequirementController {

    private final ProjectSkillRequirementService service;

    public ProjectSkillRequirementController(ProjectSkillRequirementService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ProjectSkillRequirement>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{projectId}/{skillId}")
    public ResponseEntity<ProjectSkillRequirement> findById(
            @PathVariable Long projectId,
            @PathVariable Long skillId) {

        ProjectSkillRequirementId id =
                new ProjectSkillRequirementId(projectId, skillId);

        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ProjectSkillRequirement> create(
            @RequestBody ProjectSkillRequirement requirement) {
        return ResponseEntity.ok(service.save(requirement));
    }

    @DeleteMapping("/{projectId}/{skillId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long projectId,
            @PathVariable Long skillId) {

        ProjectSkillRequirementId id =
                new ProjectSkillRequirementId(projectId, skillId);

        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}