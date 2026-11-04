package fairteam_backend.insights.application;

public class StudentNotFoundException extends RuntimeException {
    public StudentNotFoundException(long studentId) { super("No encontramos al estudiante " + studentId + " en este curso."); }
}
