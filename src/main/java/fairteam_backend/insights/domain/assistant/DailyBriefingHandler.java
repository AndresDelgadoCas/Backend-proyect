package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.CourseSummary;
import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.PeriodAverage;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/** Answers "¿cómo va el curso?" with the spoken briefing. */
final class DailyBriefingHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("resumen", "como va el curso", "como vamos", "como va todo", "que hago hoy", "que hay hoy", "buenos dias", "buenas tardes", "buenas noches", "novedades", "informame", "ponme al dia")) return Optional.empty();
        CourseSummary s = c.analysis().summary();
        AssistantReply reply = AssistantReply.say("briefing", c.briefing());
        String topic = c.analysis().weakestSubject().map(SubjectAverage::name).orElse(null);
        return Optional.of(s.low() > 0
                ? reply.withActions(Handlers.workshop("Preparar refuerzo", "workshop", Level.LOW, topic, null), ReplyAction.of("navigate", "Ver seguimiento", "route", "seguimiento"))
                : reply.withActions(ReplyAction.of("navigate", "Ver seguimiento", "route", "seguimiento")));
    }
}
