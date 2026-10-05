package fairteam_backend.strategy;

public class TeamCandidate {

    private Long studentId;
    private double academicScore;
    private double skillScore;
    private double availabilityScore;

    public TeamCandidate() {
    }

    public TeamCandidate(Long studentId,
                          double academicScore,
                          double skillScore,
                          double availabilityScore) {
        this.studentId = studentId;
        this.academicScore = academicScore;
        this.skillScore = skillScore;
        this.availabilityScore = availabilityScore;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public double getAcademicScore() {
        return academicScore;
    }

    public void setAcademicScore(double academicScore) {
        this.academicScore = academicScore;
    }

    public double getSkillScore() {
        return skillScore;
    }

    public void setSkillScore(double skillScore) {
        this.skillScore = skillScore;
    }

    public double getAvailabilityScore() {
        return availabilityScore;
    }

    public void setAvailabilityScore(double availabilityScore) {
        this.availabilityScore = availabilityScore;
    }
}