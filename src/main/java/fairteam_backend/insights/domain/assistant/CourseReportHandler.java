package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Generates the printable course report. */
final class CourseReportHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("informe", "reporte", "pdf", "imprimir")) return Optional.empty();
        return Optional.of(AssistantReply.say("report", "Generé el informe de " + c.analysis().courseName() + ". Se abrirá para imprimir o guardar como PDF.").andRun(ReplyAction.of("report", "Generar informe")));
    }
}
