package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;

/** "¿Qué me recomiendas?": the three most useful next steps for the teacher, from the data. */
final class TeacherAdviceHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("que me recomiendas", "que hago", "recomendacion", "consejo", "que deberia")) return Optional.empty();
        var a = c.analysis();
        List<String> steps = new java.util.ArrayList<>();
        List<StudentPerformance> low = a.students().stream().filter(p -> p.hasGrades() && p.level() == Level.LOW)
                .sorted(Comparator.comparingDouble(StudentPerformance::average)).toList();
        if (!low.isEmpty()) steps.add("prepara un refuerzo" + a.weakestSubject().map(x -> " de " + x.name()).orElse("") + " para " + Grades.plural(low.size(), "estudiante", "estudiantes"));
        long down = a.students().stream().filter(p -> p.direction() == Direction.DOWN).count();
        if (down > 0) steps.add("habla con " + (down == 1 ? "quien viene bajando" : "los " + down + " que vienen bajando"));
        if (a.summary().ungraded() > 0) steps.add("completa las notas de " + Grades.plural(a.summary().ungraded(), "estudiante", "estudiantes"));
        if (c.materials() == 0) steps.add("sube el material del tema para que pueda crear talleres");
        if (steps.isEmpty()) steps.add("propón un reto a los que van mejor; el curso está al día");
        String text = "Te recomiendo: " + String.join("; ", steps.subList(0, Math.min(3, steps.size()))) + ".";
        String topic = a.weakestSubject().map(x -> x.name()).orElse(null);
        return Optional.of(AssistantReply.say("teacher-advice", text)
                .withActions(Handlers.workshop("Preparar refuerzo", "workshop", Level.LOW, topic, null), ReplyAction.of("navigate", "Ver seguimiento", "route", "seguimiento")));
    }
}
