package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.StudentPerformance;

/** Lists students who improved between periods. */
final class ImprovingHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("subiendo", "mejorando", "mejoro", "mejorado", "progreso")) return Optional.empty();
        List<StudentPerformance> up = c.analysis().students().stream().filter(p -> p.direction() == Direction.UP)
                .sorted(Comparator.comparingDouble(StudentPerformance::delta).reversed()).toList();
        if (up.isEmpty()) return Optional.of(AssistantReply.say("improving", "Por ahora nadie ha subido más de " + Grades.format(0.3) + " entre cortes."));
        return Optional.of(AssistantReply.say("improving", Grades.plural(up.size(), "estudiante viene", "estudiantes vienen") + " mejorando. Vale la pena reconocérselo.")
                .withItems(up.stream().limit(6).map(p -> Handlers.item(p, "+" + Grades.format(p.delta()), Handlers.path(p))).toList()));
    }
}
