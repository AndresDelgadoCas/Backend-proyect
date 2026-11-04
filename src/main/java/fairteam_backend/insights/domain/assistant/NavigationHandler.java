package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

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
