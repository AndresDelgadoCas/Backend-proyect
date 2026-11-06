package fairteam_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 40)
    private String period;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    /** Highest grade of the course scale (5, 10, 20, 100…); grades are stored on 0–5 whatever it is. */
    @Column(name = "max_grade", nullable = false, precision = 7, scale = 2)
    private BigDecimal maxGrade;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        if (maxGrade == null) maxGrade = BigDecimal.valueOf(5);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public BigDecimal getMaxGrade() { return maxGrade == null ? null : maxGrade.stripTrailingZeros(); }
    public void setMaxGrade(BigDecimal maxGrade) { this.maxGrade = maxGrade; }
}
