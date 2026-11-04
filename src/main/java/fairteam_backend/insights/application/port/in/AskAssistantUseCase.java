package fairteam_backend.insights.application.port.in;

import fairteam_backend.insights.domain.assistant.AssistantReply;

/** Inbound port: answer a teacher's question about a course in natural Spanish. */
public interface AskAssistantUseCase {
    AssistantReply ask(long courseId, String question, String teacherName);
}
