package fairteam_backend.insights.application.port.in;

import java.util.List;

import fairteam_backend.insights.domain.assistant.AssistantReply;
import fairteam_backend.insights.domain.assistant.Turn;

/** Inbound port: answer a teacher's question about a course in natural Spanish, remembering the conversation. */
public interface AskAssistantUseCase {
    AssistantReply ask(long courseId, String question, String teacherName);

    default AssistantReply ask(long courseId, String question, String teacherName, List<Turn> history) {
        return ask(courseId, question, teacherName);
    }
}
