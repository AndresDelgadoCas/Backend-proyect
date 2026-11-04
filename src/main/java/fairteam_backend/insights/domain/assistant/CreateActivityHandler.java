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

/** Prepares a workshop or quiz request (mode, level, topic, student). */
final class CreateActivityHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("crea", "genera", "prepara", "haz", "hazme", "arma", "disena", "quiero un", "necesito un")) return Optional.empty();
        if (!q.has("taller", "quiz", "evaluacion", "actividad", "examen", "prueba", "guia", "refuerzo")) return Optional.empty();
        String mode = q.has("quiz", "evaluacion", "examen", "prueba") ? "quiz" : "workshop";
        StudentPerformance student = q.student();
        Level level = student != null ? student.level() : q.level() != null ? q.level() : c.analysis().summary().low() > 0 ? Level.LOW : Level.MEDIUM;
        if (level == Level.NO_DATA) level = Level.MEDIUM;
        String topic = q.subject() != null ? q.subject().name() : null;
        String what = mode.equals("quiz") ? "un quiz de tres preguntas" : "un taller de práctica";
        String who = student != null ? " para " + Grades.firstName(student.student().name()) : " " + Handlers.levelPhrase(level);
        ReplyAction open = Handlers.workshop("Abrir talleres", mode, level, topic, student);
        if (c.materials() == 0) return Optional.of(AssistantReply.say("create-activity", "Para crear " + what + " necesito el material de clase. Súbelo en Talleres y lo preparo.").andRun(open));
        return Optional.of(AssistantReply.say("create-activity", "Voy a preparar " + what + (topic != null ? " sobre " + topic : "") + who + ". Revisa el material seleccionado y presiona crear.").andRun(open));
    }
}
