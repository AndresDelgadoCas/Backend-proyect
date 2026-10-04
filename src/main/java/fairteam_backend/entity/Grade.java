package fairteam_backend.entity;
import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "grades",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uq_student_subject_period",
            columnNames = {"student_id", "subject_id", "period"}
        )
    }
)
public class Grade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column(nullable = false, precision = 3, scale = 2)
    private BigDecimal grade;

    @Column(nullable = false, length = 20)
    private String period;

    public Grade() {
    }

    public Grade(Long id, Long studentId, Long subjectId, Double grade, String period) {
        this.id = id;

        Student student = new Student();
        student.setId(studentId);
        this.student = student;

        Subject subject = new Subject();
        subject.setId(subjectId);
        this.subject = subject;

        this.grade = BigDecimal.valueOf(grade);
        this.period = period;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Subject getSubject() {
        return subject;
    }

    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    public BigDecimal getGrade() {
        return grade;
    }

    public void setGrade(BigDecimal grade) {
        this.grade = grade;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    @Transient
    public Long getStudentId() {
        return student != null ? student.getId() : null;
    }

    @Transient
    public Long getSubjectId() {
        return subject != null ? subject.getId() : null;
    }
}