package fairteam_backend.insights.domain.feedback;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.StudentPerformance;

class WarmFeedback extends FeedbackTemplate {
    @Override protected String greeting(StudentPerformance student) { return "Hola, " + Grades.firstName(student.student().name()) + ":"; }
    @Override protected String closing() { return "Un saludo,"; }
}
