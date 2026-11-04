package fairteam_backend.insights.application.port.in;

import fairteam_backend.insights.domain.feedback.FeedbackTone;

/** Inbound port: a personal feedback message for one student, written from their grades. */
public interface DraftFeedbackUseCase {
    record Feedback(long studentId, String studentName, String email, String subject, String body, FeedbackTone tone) { }

    Feedback draft(long courseId, long studentId, FeedbackTone tone, String teacherName);
}
