package fairteam_backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "project_skill_requirements")
public class ProjectSkillRequirement {

    @EmbeddedId
    private ProjectSkillRequirementId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("projectId")
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Column(name = "min_level", nullable = false)
    private Integer minLevel;

    public ProjectSkillRequirement() {
    }

    public ProjectSkillRequirementId getId() {
        return id;
    }

    public void setId(ProjectSkillRequirementId id) {
        this.id = id;
    }

    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Integer getMinLevel() {
        return minLevel;
    }

    public void setMinLevel(Integer minLevel) {
        this.minLevel = minLevel;
    }
}