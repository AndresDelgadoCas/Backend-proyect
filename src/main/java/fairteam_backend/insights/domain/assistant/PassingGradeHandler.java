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

/** Answers what grade a student needs to pass, or who may fail. */
final class PassingGradeHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("que nota", "cuanto debe", "cuanto necesita", "cuanto le falta", "necesita para", "necesita sacar", "para pasar", "para aprobar", "alcanza", "pierde", "perder", "aprueba", "gana el")) return Optional.empty();
        ReplyAction simulator = ReplyAction.of("navigate", "Abrir simulador", "route", "seguimiento", "vista", "simulador");
        if (q.student() != null) {
            StudentPerformance s = q.student();
            GradeProjection p = c.projections().get(s.student().id());
            return Optional.of(AssistantReply.say("passing-grade", s.student().name() + ": " + (p != null ? p.explanation() : "no tengo suficientes notas."))
                    .withActions(ReplyAction.of("review", "Ver ficha de " + Grades.firstName(s.student().name()), "studentId", String.valueOf(s.student().id())), simulator));
        }
        List<GradeProjection> atRisk = c.projections().values().stream()
                .filter(p -> p.status() == ProjectionStatus.HARD || p.status() == ProjectionStatus.IMPOSSIBLE)
                .sorted(Comparator.comparing((GradeProjection p) -> p.needed() == null ? 9 : p.needed()).reversed()).toList();
        if (atRisk.isEmpty()) return Optional.of(AssistantReply.say("passing-grade", "Con las notas actuales, todos pueden llegar a 3,0 sin necesitar más de 3,5 en lo que falta.").withActions(simulator));
        List<ReplyItem> items = atRisk.stream().limit(6).map(p -> c.analysis().student(p.studentId())
                .map(s -> new ReplyItem(p.studentId(), s.student().name(), p.needed() == null ? "—" : Grades.format(p.needed()), p.explanation(), "low")).orElse(null))
                .filter(java.util.Objects::nonNull).toList();
        return Optional.of(AssistantReply.say("passing-grade", Grades.plural(atRisk.size(), "estudiante está", "estudiantes están") + " en riesgo de perder: necesitan más de 3,5 en lo que falta.")
                .withItems(items).withActions(simulator));
    }
}
