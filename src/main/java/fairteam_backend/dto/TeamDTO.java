package fairteam_backend.dto;

import java.math.BigDecimal;

public class TeamDTO {

    private Long id;
    private Long projectId;
    private String name;
    private String formationStrategy;
    private BigDecimal score;
    private String explanation;

    public TeamDTO() {
    }

    public TeamDTO(Long id, Long projectId, String name,
                    String formationStrategy, BigDecimal score,
                    String explanation) {
        this.id = id;
        this.projectId = projectId;
        this.name = name;
        this.formationStrategy = formationStrategy;
        this.score = score;
        this.explanation = explanation;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProjectId() {
        return projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFormationStrategy() {
        return formationStrategy;
    }

    public void setFormationStrategy(String formationStrategy) {
        this.formationStrategy = formationStrategy;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }
}