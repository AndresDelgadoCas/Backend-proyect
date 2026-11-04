package fairteam_backend.insights.domain.feedback;

import fairteam_backend.insights.domain.StudentPerformance;

class FormalFeedback extends FeedbackTemplate {
    @Override protected String greeting(StudentPerformance student) { return "Estimado(a) " + student.student().name() + ":"; }
    @Override protected String closing() { return "Cordialmente,"; }
}
