package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;

class HowToHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    private String answer(String question) {
        AssistantReply reply = AssistantEngine.standard().answer(question, ctx);
        if (!"how-to".equals(reply.intent())) return reply.intent();
        return AppManual.entries().stream().filter(e -> e.answer().equals(reply.text())).findFirst().orElseThrow().id();
    }

    @Test
    void answersHowToQuestionsAboutTheWholeApp() {
        Map<String, String> bank = Map.ofEntries(
                Map.entry("¿Cómo creo un curso nuevo?", "crear-curso"),
                Map.entry("como agrego una materia", "crear-curso"),
                Map.entry("¿Cómo puedo borrar un curso?", "editar-curso"),
                Map.entry("¿cómo agrego estudiantes?", "agregar-estudiantes"),
                Map.entry("¿Cómo importo la lista desde Excel?", "importar-lista"),
                Map.entry("¿cómo importo las notas de excel?", "importar-notas"),
                Map.entry("¿Cómo pongo porcentajes a las columnas?", "porcentajes"),
                Map.entry("¿dónde cambio la escala a sobre 10?", "escala"),
                Map.entry("¿cómo ordeno las columnas?", "columnas"),
                Map.entry("¿Cómo saco la nota definitiva?", "cortes"),
                Map.entry("¿Cómo tomo asistencia?", "asistencia"),
                Map.entry("¿Cómo puedo crear un taller?", "taller"),
                Map.entry("como hago un quiz", "quiz"),
                Map.entry("¿Dónde subo el material del curso?", "material"),
                Map.entry("¿dónde quedan mis talleres guardados?", "guardados"),
                Map.entry("¿Cómo hago una rúbrica?", "rubrica"),
                Map.entry("¿cómo califico con la rúbrica?", "calificar"),
                Map.entry("¿Cómo paso las notas de la rúbrica al libro?", "llevar-libro"),
                Map.entry("¿Cómo armo equipos?", "equipos"),
                Map.entry("¿Cómo pongo el modo oscuro?", "tema-oscuro"),
                Map.entry("¿Cómo cierro sesión?", "cerrar-sesion"),
                Map.entry("¿qué atajos de teclado hay?", "atajos"),
                Map.entry("¿Cómo se usa la app?", "tutorial"),
                Map.entry("¿la IA ve los nombres de mis estudiantes? ¿qué se envía?", "privacidad"),
                Map.entry("¿Cómo le escribo un correo a un estudiante?", "mensaje"));
        var wrong = new java.util.TreeMap<String, String>();
        bank.forEach((question, id) -> { String got = answer(question); if (!got.equals(id)) wrong.put(question, got + " (esperaba " + id + ")"); });
        assertThat(wrong).isEmpty();
    }

    @Test
    void ordersAndDataQuestionsAreNotHowTo() {
        assertThat(answer("crea un taller de funciones")).isEqualTo("create-activity");
        assertThat(answer("¿cómo va el curso?")).isNotEqualTo("how-to");
        assertThat(answer("¿cómo va Valentina?")).isEqualTo("student-status");
        assertThat(answer("¿quién viene bajando?")).isEqualTo("falling-behind");
    }

    @Test
    void offersToDoItAndToTakeTheTeacherThere() {
        AssistantReply reply = AssistantEngine.standard().answer("¿Cómo creo un quiz?", ctx);
        assertThat(reply.actions()).extracting(ReplyAction::label).containsExactly("Hazlo por mí", "Llévame");
        assertThat(reply.actions().get(0).params()).containsEntry("mode", "quiz").containsEntry("auto", "true").containsKey("topic");
        assertThat(reply.actions().get(1).params()).containsEntry("route", "talleres").containsEntry("resaltar", "crear");
        AssistantReply tutorial = AssistantEngine.standard().answer("¿cómo funciona la app?", ctx);
        assertThat(tutorial.run().kind()).isEqualTo("guide");
    }

    @Test
    void theManualIsWhatTheAiReads() {
        assertThat(AppManual.text()).contains("[porcentajes] Porcentajes de las columnas", "[llevar-libro]");
        assertThat(AppManual.entries()).extracting(AppManual.HowTo::id).doesNotHaveDuplicates();
    }
}
