package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.StudentPerformance;

/** Lists students whose average dropped between periods. */
final class FallingBehindHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("bajando", "bajado", "empeor", "cayendo", "riesgo", "preocup", "alerta")) return Optional.empty();
        ReplyAction trends = ReplyAction.of("navigate", "Ver tendencias", "route", "seguimiento", "vista", "tendencia");
        List<StudentPerformance> down = c.analysis().students().stream().filter(p -> p.direction() == Direction.DOWN)
                .sorted(Comparator.comparingDouble(StudentPerformance::delta)).toList();
        if (down.isEmpty()) {
            boolean twoPeriods = c.analysis().periods().size() >= 2;
            return Optional.of(AssistantReply.say("falling-behind", twoPeriods ? "Buenas noticias: nadie ha bajado más de 0,3 entre cortes." : "Necesito notas de al menos dos cortes para ver tendencias.").withActions(trends));
        }
        return Optional.of(AssistantReply.say("falling-behind", Grades.plural(down.size(), "estudiante viene", "estudiantes vienen") + " bajando. " + Grades.firstName(down.get(0).student().name()) + " es quien más bajó.")
                .withItems(down.stream().limit(6).map(p -> Handlers.item(p, "−" + Grades.format(Math.abs(p.delta())), Handlers.path(p))).toList()).withActions(trends));
    }
}
