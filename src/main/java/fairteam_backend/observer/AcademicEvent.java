package fairteam_backend.observer;

public class AcademicEvent {

    private final Long studentId;
    private final double newGrade;

    public AcademicEvent(Long studentId, double newGrade) {
        this.studentId = studentId;
        this.newGrade = newGrade;
    }

    public Long getStudentId() {
        return studentId;
    }

    public double getNewGrade() {
        return newGrade;
    }
}