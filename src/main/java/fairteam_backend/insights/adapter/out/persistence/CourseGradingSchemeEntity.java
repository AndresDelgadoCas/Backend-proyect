package fairteam_backend.insights.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

/** JPA row for a course's grading scheme. teacher_id is filled by the tenant trigger (see V7). */
@Entity
@Table(name = "course_grading_schemes")
public class CourseGradingSchemeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_id", nullable = false, unique = true)
    private Long courseId;

    /** Weights in percent separated by commas, e.g. "30,30,40". */
    @Column(nullable = false, length = 60)
    private String weights;

    @Column(name = "passing_grade", nullable = false, precision = 3, scale = 1)
    private BigDecimal passingGrade;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist @PreUpdate
    void touch() { updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }
    public String getWeights() { return weights; }
    public void setWeights(String weights) { this.weights = weights; }
    public BigDecimal getPassingGrade() { return passingGrade; }
    public void setPassingGrade(BigDecimal passingGrade) { this.passingGrade = passingGrade; }
}
