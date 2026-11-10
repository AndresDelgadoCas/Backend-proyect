package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/**
 * "Hazme un taller", "crea un quiz de ciclos para Valentina": Búho does not just open the page, it asks the
 * app to create the activity right away ({@code auto=true}). Without a topic it uses the one that costs the
 * course the most; without material it writes from the topic alone.
 */
final class CreateActivityHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("crea", "genera", "prepara", "haz", "hazme", "arma", "disena", "quiero un", "necesito un", "hagamos")) return Optional.empty();
        if (!q.has("taller", "quiz", "evaluacion", "actividad", "examen", "prueba", "guia", "refuerzo", "ejercicios")) return Optional.empty();
        if (q.has("rubrica")) return Optional.empty();
        String mode = q.has("quiz", "evaluacion", "examen", "prueba") ? "quiz" : "workshop";
        StudentPerformance student = q.student();
        Level level = student != null ? student.level() : q.level() != null ? q.level() : c.analysis().summary().low() > 0 ? Level.LOW : Level.MEDIUM;
        if (level == Level.NO_DATA) level = Level.MEDIUM;
        String topic = q.subject() != null ? q.subject().name()
                : student != null && student.weakest() != null ? student.weakest().name()
                : c.analysis().weakestSubject().map(SubjectAverage::name).orElse(null);
        String what = mode.equals("quiz") ? "un quiz" : "un taller de práctica";
        String who = student != null ? " para " + Grades.firstName(student.student().name()) : " " + Handlers.levelPhrase(level);
        ReplyAction create = Handlers.workshop("Ver en Talleres", mode, level, topic, student).with("auto", "true");
        String source = c.materials() == 0 ? " Como el curso no tiene material, lo escribo a partir del tema." : " Uso el material más reciente del curso.";
        return Optional.of(AssistantReply.say("create-activity",
                "Listo, estoy creando " + what + (topic != null ? " sobre " + topic : "") + who + "." + source
                        + " Lo verás en Talleres en unos segundos y queda guardado.").andRun(create));
    }
}
