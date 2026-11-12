package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorstStudentsHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void aStudentQuestionGetsAStudentEvenWithTypos() {
        for (String question : new String[] { "que estudinate va peor en esta materia", "¿Qué estudiante va peor en esta materia?", "¿quién tiene el promedio más bajo?", "cuál es el peor alumno del curso" }) {
            AssistantReply reply = AssistantEngine.standard().answer(question, ctx);
            assertThat(reply.intent()).as(question).isEqualTo("worst-students");
            assertThat(reply.text()).startsWith("Valentina Rojas tiene el promedio más bajo");
            assertThat(reply.items()).first().satisfies(item -> assertThat(item.name()).isEqualTo("Valentina Rojas"));
        }
    }

    @Test
    void withinATopicItRanksByThatTopic() {
        AssistantReply reply = AssistantEngine.standard().answer("¿qué estudiante va peor en funciones?", ctx);
        assertThat(reply.text()).contains("en Funciones");
    }

    @Test
    void topicQuestionsStillAnswerWithTheTopic() {
        assertThat(AssistantEngine.standard().answer("¿qué tema les cuesta más a los estudiantes?", ctx).intent()).isEqualTo("subject-extremes");
        assertThat(AssistantEngine.standard().answer("¿cuál es la materia más difícil?", ctx).intent()).isEqualTo("subject-extremes");
        assertThat(AssistantEngine.standard().answer("¿qué estudiante va mejor en esta materia?", ctx).intent()).isEqualTo("best");
    }
}
