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

/** Explains what the assistant can do. */
final class HelpHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("ayuda", "que puedes", "que sabes", "comandos", "como funcionas")) return Optional.empty();
        return Optional.of(AssistantReply.say("help", "Puedo decirte cómo va el curso, quién necesita apoyo y quién viene bajando, calcular qué nota necesita alguien para pasar, crear talleres y quizzes, redactar mensajes a tus estudiantes y generar el informe del curso. Pregúntame como le preguntarías a un colega."));
    }
}
