package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/**
 * "Hazme un taller", "crea un quiz de derivadas con 5 preguntas para Valentina": Búho does not just open the
 * page, it asks the app to create the activity right away ({@code auto=true}). The topic is the course topic
 * the teacher named, or the phrase after "de"/"sobre" when it is not one of the course's topics yet, and only
 * without either the one that costs the course the most. The request itself travels as the teacher's
 * instructions, so details like "en parejas" or "con 5 preguntas" are not lost.
 */
final class CreateActivityHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("crea", "genera", "prepara", "haz", "hazme", "arma", "disena", "quiero un", "necesito un", "hagamos")) return Optional.empty();
        if (!q.has("taller", "quiz", "evaluacion", "actividad", "examen", "prueba", "guia", "refuerzo", "ejercicios")) return Optional.empty();
        if (q.has("rubrica")) return Optional.empty();
        String mode = q.has("quiz", "evaluacion", "examen", "prueba") ? "quiz" : "workshop";
        StudentPerformance student = q.student();
        Level level = q.level() != null ? q.level() : student != null ? student.level() : c.analysis().summary().low() > 0 ? Level.LOW : Level.MEDIUM;
        if (level == Level.NO_DATA) level = Level.MEDIUM;
        String topic = topic(q, c, student);
        String kindWord = mode.equals("quiz") ? "un quiz" : "un taller";
        String what = kindWord + switch (level) { case LOW -> " de refuerzo"; case HIGH -> " de reto"; default -> " de práctica"; };
        String who = student != null ? " para " + Grades.firstName(student.student().name()) : "";
        ReplyAction create = Handlers.workshop("Ver en Talleres", mode, level, topic, student).with("auto", "true").with("prompt", q.raw());
        String source = c.materials() == 0 ? " Como el curso no tiene material, lo escribo a partir del tema." : " Uso el material del curso.";
        return Optional.of(AssistantReply.say("create-activity",
                "Listo, estoy creando " + what + (topic != null ? " sobre " + topic : "") + who + "." + source
                        + " Lo verás en Talleres en unos segundos y queda guardado.").andRun(create));
    }

    /**
     * The phrase the teacher wrote wins over a course topic that only shares a word with it ("el patrón
     * Decorator" is not "Patrones de comportamiento"); a course topic the phrase names is used by its full name.
     */
    static String topic(AssistantQuery q, AssistantContext c, StudentPerformance student) {
        var phrase = TopicPhrase.in(q.raw())
                .filter(t -> student == null || !AssistantQuery.normalize(student.student().name()).contains(AssistantQuery.normalize(t)));
        if (phrase.isPresent()) {
            String said = AssistantQuery.normalize(phrase.get());
            for (SubjectAverage s : c.analysis().subjects()) {
                String name = AssistantQuery.normalize(s.name());
                if (name.equals(said) || name.startsWith(said) || said.startsWith(name)) return s.name();
            }
            if (q.subject() == null || !said.contains(AssistantQuery.normalize(q.subject().name()).split(" ")[0])) return phrase.get();
        }
        if (q.subject() != null) return q.subject().name();
        return student != null && student.weakest() != null ? student.weakest().name()
                : c.analysis().weakestSubject().map(SubjectAverage::name).orElse(null);
    }
}
