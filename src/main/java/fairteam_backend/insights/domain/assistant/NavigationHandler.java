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

/** Opens a screen of the app. */
final class NavigationHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        String[][] routes = {
                {"equipo|grupos de trabajo", "Abro el armador de equipos.", "equipos", null},
                {"importa|subir notas|cargar notas|sube las notas|subir la lista|cargar la lista|estudiantes", "Abro Estudiantes y notas para que importes el Excel.", "estudiantes", null},
                {"material|archivo", "Abro Talleres: ahí puedes subir el material del curso.", "talleres", null},
                {"simulador|proyeccion", "Abro el simulador de notas.", "seguimiento", "simulador"},
                {"seguimiento|alertas", "Abro el seguimiento.", "seguimiento", null},
                {"ajustes|configuracion", "Abro los ajustes.", "ajustes", null},
                {"inicio|principal", "Volvemos al inicio.", "inicio", null},
        };
        for (String[] route : routes) {
            if (q.has(route[0].split("\\|"))) {
                return Optional.of(AssistantReply.say("navigate", route[1]).andRun(ReplyAction.of("navigate", route[2], "route", route[2], "vista", route[3])));
            }
        }
        return Optional.empty();
    }
}
