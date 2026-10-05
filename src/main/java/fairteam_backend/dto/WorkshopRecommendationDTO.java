package fairteam_backend.dto;

import java.math.BigDecimal;

public class WorkshopRecommendationDTO {

    private Long id;
    private Long studentId;
    private Long workshopId;
    private String reason;
    private BigDecimal recommendationScore;

    public WorkshopRecommendationDTO() {
    }

    public WorkshopRecommendationDTO(Long id, Long studentId,
                                      Long workshopId, String reason,
                                      BigDecimal recommendationScore) {
        this.id = id;
        this.studentId = studentId;
        this.workshopId = workshopId;
        this.reason = reason;
        this.recommendationScore = recommendationScore;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getWorkshopId() {
        return workshopId;
    }

    public void setWorkshopId(Long workshopId) {
        this.workshopId = workshopId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public BigDecimal getRecommendationScore() {
        return recommendationScore;
    }

    public void setRecommendationScore(BigDecimal recommendationScore) {
        this.recommendationScore = recommendationScore;
    }
}