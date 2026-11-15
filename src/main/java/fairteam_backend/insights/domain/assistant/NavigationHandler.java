package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Opens a screen of the app. */
final class NavigationHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        String[][] routes = {
                {"equipo|grupos de trabajo", "Abro el armador de equipos.", "teams", null},
                {"importa|subir notas|cargar notas|sube las notas|subir la lista|cargar la lista|estudiantes", "Abro Estudiantes y notas para que importes el Excel.", "students", null},
                {"material|archivo", "Abro Talleres: ahí puedes subir el material del curso.", "workshops", null},
                {"simulador|proyeccion", "Abro el simulador de notas.", "follow-up", "simulator"},
                {"seguimiento|alertas", "Abro el seguimiento.", "follow-up", null},
                {"ajustes|configuracion", "Abro los ajustes.", "settings", null},
                {"inicio|principal", "Volvemos al inicio.", "home", null},
        };
        for (String[] route : routes) {
            if (q.has(route[0].split("\\|"))) {
                return Optional.of(AssistantReply.say("navigate", route[1]).andRun(ReplyAction.of("navigate", "Abrir", "route", route[2], "view", route[3])));
            }
        }
        return Optional.empty();
    }
}
