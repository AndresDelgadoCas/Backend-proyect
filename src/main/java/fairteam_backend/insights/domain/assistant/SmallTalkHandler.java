package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

import fairteam_backend.insights.domain.Grades;

/** Courtesy: answers greetings and thanks briefly, so the conversation feels natural. */
final class SmallTalkHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        String t = q.text();
        if (t.matches("(hola|hoal|ola|hey|buenas|buenos dias|buenas tardes|buenas noches|que tal|saludos)( [a-zñ]+){0,3}") && !CourseTopicGate.aboutCourse(q)) return Optional.of(AssistantReply.say("small-talk", "¡Hola" + (Grades.teacherFirstName(c.teacher()).isEmpty() ? "" : ", " + Grades.teacherFirstName(c.teacher())) + "! ¿Quieres que te cuente cómo va " + c.analysis().courseName() + "?")
                .withActions(ReplyAction.of("briefing", "Escuchar el resumen")));
        if (q.has("gracias", "muchas gracias", "te agradezco")) return Optional.of(AssistantReply.say("small-talk", "Con gusto. Aquí estoy cuando me necesites."));
        return Optional.empty();
    }
}
