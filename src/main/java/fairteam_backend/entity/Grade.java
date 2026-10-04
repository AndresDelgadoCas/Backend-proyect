package fairteam_backend.entity;

public class Grade {

    private Long id;
    private Long studentId;
    private Long subjectId;
    private Double grade;
    private String period;

    public Grade() {
    }

    public Grade(Long id, Long studentId, Long subjectId, Double grade, String period) {
        this.id = id;
        this.studentId = studentId;
        this.subjectId = subjectId;
        this.grade = grade;
        this.period = period;
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

    public Long getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Long subjectId) {
        this.subjectId = subjectId;
    }

    public Double getGrade() {
        return grade;
    }

    public void setGrade(Double grade) {
        this.grade = grade;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
}