package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/** Lists students by level, optionally within a subject. */
final class WhoNeedsHelpHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("quien", "quienes", "cuales", "cuantos", "lista") && q.level() == null && q.subject() == null) return Optional.empty();
        SubjectAverage subject = q.subject();
        if (subject != null) {
            record Hit(StudentPerformance p, double avg) { }
            List<Hit> hits = c.analysis().students().stream()
                    .map(p -> p.subjects().stream().filter(x -> x.subjectId() == subject.subjectId()).findFirst().map(x -> new Hit(p, x.average())).orElse(null))
                    .filter(h -> h != null && Grades.round1(h.avg()) < Grades.PASSING).sorted(Comparator.comparingDouble(Hit::avg)).toList();
            String text = hits.isEmpty()
                    ? "En " + subject.name() + " nadie está por debajo de 3,0. Promedio del tema: " + Grades.format(subject.average()) + "."
                    : "En " + subject.name() + ", " + Grades.plural(hits.size(), "estudiante está", "estudiantes están") + " por debajo de 3,0. Promedio del tema: " + Grades.format(subject.average()) + ".";
            AssistantReply reply = AssistantReply.say("who-needs-help", text)
                    .withItems(hits.stream().limit(8).map(h -> new ReplyItem(h.p().student().id(), h.p().student().name(), Grades.format(h.avg()), "promedio general " + Grades.format(h.p().average()), "low")).toList());
            return Optional.of(hits.isEmpty() ? reply : reply.withActions(Handlers.workshop("Taller de " + subject.name(), "workshop", Level.LOW, subject.name(), null)));
        }
        Level level = q.level() == null ? Level.LOW : q.level();
        List<StudentPerformance> list = c.analysis().students().stream().filter(p -> p.hasGrades() && p.level() == level)
                .sorted(Comparator.comparingDouble(StudentPerformance::average)).toList();
        String many = switch (level) { case LOW -> "necesitan refuerzo"; case MEDIUM -> "están en seguimiento"; default -> "van con buen progreso"; };
        String one = switch (level) { case LOW -> "necesita refuerzo"; case MEDIUM -> "está en seguimiento"; default -> "va con buen progreso"; };
        if (list.isEmpty()) return Optional.of(AssistantReply.say("who-needs-help", "Nadie " + one + " en este momento."));
        String text = list.size() == 1 ? "Solo " + list.get(0).student().name() + " " + one + "." : list.size() + " estudiantes " + many + ".";
        AssistantReply reply = AssistantReply.say("who-needs-help", text)
                .withItems(list.stream().limit(8).map(p -> Handlers.item(p, Grades.format(p.average()), Grades.plural(p.gradeCount(), "nota", "notas"))).toList());
        String topic = c.analysis().weakestSubject().map(SubjectAverage::name).orElse(null);
        return Optional.of(level == Level.LOW ? reply.withActions(Handlers.workshop("Preparar refuerzo", "workshop", Level.LOW, topic, null)) : reply);
    }
}
