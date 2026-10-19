
package fairteam_backend.entity.evaluation;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "ai_evaluations")
public class AiEvaluation {

    public enum EvaluationStatus {
        PENDING,
        SUBMITTED,
        PROCESSING,
        PROPOSED,
        APPROVED,
        REJECTED,
        FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false, unique = true)
    private AssignmentSubmission submission;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EvaluationStatus status = EvaluationStatus.PENDING;

    @Column(name = "suggested_grade", precision = 5, scale = 2)
    private BigDecimal suggestedGrade;

    @Column(name = "feedback", columnDefinition = "TEXT")
    private String feedback;

    @Column(name = "criteria_results", nullable = false, columnDefinition = "jsonb")
    private String criteriaResults = "{}";

    @Column(name = "reviewed_by")
    private Long reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public AiEvaluation() {
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = EvaluationStatus.PENDING;
        }
        if (criteriaResults == null) {
            criteriaResults = "{}";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AssignmentSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(AssignmentSubmission submission) {
        this.submission = submission;
    }

    public EvaluationStatus getStatus() {
        return status;
    }

    public void setStatus(EvaluationStatus status) {
        this.status = status;
    }

    public BigDecimal getSuggestedGrade() {
        return suggestedGrade;
    }

    public void setSuggestedGrade(BigDecimal suggestedGrade) {
        this.suggestedGrade = suggestedGrade;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public String getCriteriaResults() {
        return criteriaResults;
    }

    public void setCriteriaResults(String criteriaResults) {
        this.criteriaResults = criteriaResults;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
