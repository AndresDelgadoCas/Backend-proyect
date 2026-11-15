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
                Map.entry("¿Cómo creo un curso nuevo?", "create-course"),
                Map.entry("como agrego una materia", "create-course"),
                Map.entry("¿Cómo puedo borrar un curso?", "edit-course"),
                Map.entry("¿cómo agrego estudiantes?", "add-students"),
                Map.entry("¿Cómo importo la lista desde Excel?", "import-roster"),
                Map.entry("¿cómo importo las notas de excel?", "import-grades"),
                Map.entry("¿Cómo pongo porcentajes a las columnas?", "weights"),
                Map.entry("¿dónde cambio la escala a sobre 10?", "scale"),
                Map.entry("¿cómo ordeno las columnas?", "columns"),
                Map.entry("¿Cómo saco la nota definitiva?", "periods"),
                Map.entry("¿Cómo tomo asistencia?", "attendance"),
                Map.entry("¿Cómo puedo crear un taller?", "workshop"),
                Map.entry("como hago un quiz", "quiz"),
                Map.entry("¿Dónde subo el material del curso?", "material"),
                Map.entry("¿dónde quedan mis talleres guardados?", "saved"),
                Map.entry("¿Cómo hago una rúbrica?", "rubric"),
                Map.entry("¿cómo califico con la rúbrica?", "grade"),
                Map.entry("¿Cómo paso las notas de la rúbrica al libro?", "to-gradebook"),
                Map.entry("¿Cómo armo equipos?", "teams"),
                Map.entry("¿Cómo pongo el modo oscuro?", "dark-theme"),
                Map.entry("¿Cómo cierro sesión?", "sign-out"),
                Map.entry("¿qué atajos de teclado hay?", "shortcuts"),
                Map.entry("¿Cómo se usa la app?", "tutorial"),
                Map.entry("¿la IA ve los nombres de mis estudiantes? ¿qué se envía?", "privacy"),
                Map.entry("¿Cómo le escribo un correo a un estudiante?", "message"));
        var wrong = new java.util.TreeMap<String, String>();
        bank.forEach((question, id) -> { String got = answer(question); if (!got.equals(id)) wrong.put(question, got + " (expected " + id + ")"); });
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
        assertThat(reply.actions().get(1).params()).containsEntry("route", "workshops").containsEntry("highlight", "create");
        AssistantReply tutorial = AssistantEngine.standard().answer("¿cómo funciona la app?", ctx);
        assertThat(tutorial.run().kind()).isEqualTo("guide");
    }

    @Test
    void theManualIsWhatTheAiReads() {
        assertThat(AppManual.text()).contains("[weights] Porcentajes de las columnas", "[to-gradebook]");
        assertThat(AppManual.entries()).extracting(AppManual.HowTo::id).doesNotHaveDuplicates();
    }

    @Test
    void whereToSeeWhoNeedsSupportPointsToFollowUpNotPrivacy() {
        AssistantContext c = AssistantFixture.context();
        assertThat(AppManual.find(AssistantFixture.query("donde veo quien necesita apoyo", c)).map(AppManual.HowTo::id)).contains("follow-up");
    }

    @Test
    void generatingAndDownloadingAWorkshopIsOneAnswer() {
        assertThat(AppManual.find(AssistantFixture.query("como genero un taller y lo descargo", AssistantFixture.context())).map(AppManual.HowTo::id)).contains("workshop");
    }
}
