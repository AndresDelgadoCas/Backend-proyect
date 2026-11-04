package fairteam_backend.insights.domain.feedback;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.StudentPerformance;

/** A third tone: same structure as the others, warmer opening and closing. */
class EncouragingFeedback extends FeedbackTemplate {
    @Override protected String greeting(StudentPerformance student) { return "¡Hola, " + Grades.firstName(student.student().name()) + "! Quiero contarte cómo vas:"; }
    @Override protected String closing() { return "¡Cuentas conmigo!"; }
}
