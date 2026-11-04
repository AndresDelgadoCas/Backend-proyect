package fairteam_backend.insights.application;

/** The course does not exist or belongs to another teacher (row level security hides it). */
public class CourseNotFoundException extends RuntimeException {
    public CourseNotFoundException(long courseId) { super("No encontramos el curso " + courseId + "."); }
}
