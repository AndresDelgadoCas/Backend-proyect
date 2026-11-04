package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/** Courtesy: answers greetings and thanks briefly, so the conversation feels natural. */
final class SmallTalkHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        String t = q.text();
        if (t.matches("(hola|hey|buenas|que tal)( buho)?")) return Optional.of(AssistantReply.say("small-talk", "¡Hola, " + Grades.firstName(c.teacher()) + "! ¿Quieres que te cuente cómo va " + c.analysis().courseName() + "?")
                .withActions(ReplyAction.of("briefing", "Escuchar el resumen")));
        if (q.has("gracias", "muchas gracias", "te agradezco")) return Optional.of(AssistantReply.say("small-talk", "Con gusto. Aquí estoy cuando me necesites."));
        return Optional.empty();
    }
}
