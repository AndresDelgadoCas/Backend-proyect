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

/** Generates the printable course report. */
final class CourseReportHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("informe", "reporte", "pdf", "imprimir")) return Optional.empty();
        return Optional.of(AssistantReply.say("report", "Generé el informe de " + c.analysis().courseName() + ". Se abrirá para imprimir o guardar como PDF.").andRun(ReplyAction.of("report", "Generar informe")));
    }
}
