package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/** Answers "¿por qué X está en riesgo?" with the evidence: average, trend, weakest subject and projection. */
final class ExplainRiskHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("por que", "porque", "explica", "razon")) return Optional.empty();
        StudentPerformance s = q.student();
        if (s == null) return Optional.empty();
        if (!s.hasGrades()) return Optional.of(AssistantReply.say("explain-risk", s.student().name() + " aparece porque todavía no tiene notas; no puedo saber cómo va."));
        StringBuilder text = new StringBuilder(s.student().name()).append(" tiene riesgo ").append(s.risk() >= 60 ? "alto" : s.risk() >= 35 ? "medio" : "bajo")
                .append(" (").append(s.risk()).append(" de 100) porque su promedio es ").append(Grades.format(s.average()));
        if (s.direction() == Direction.DOWN) text.append(" y bajó ").append(Grades.format(Math.abs(s.delta()))).append(" entre cortes");
        if (s.direction() == Direction.UP) text.append(", aunque viene subiendo");
        text.append('.');
        if (s.weakest() != null) text.append(" Donde más le cuesta es ").append(s.weakest().name()).append('.');
        GradeProjection p = c.projections().get(s.student().id());
        if (p != null && p.status() != ProjectionStatus.NO_DATA) text.append(' ').append(p.explanation());
        return Optional.of(AssistantReply.say("explain-risk", text.toString())
                .withActions(ReplyAction.of("review", "Ver ficha", "studentId", String.valueOf(s.student().id()))));
    }
}
