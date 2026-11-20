package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CoursesListHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void countsTheTeachersCoursesInsteadOfTalkingAboutStudents() {
        AssistantReply reply = AssistantEngine.standard().answer("cuantos cursos tengo", ctx);
        assertThat(reply.intent()).isEqualTo("courses-list");
        assertThat(reply.text()).startsWith("Tienes ").contains("curso");
    }

    @Test
    void doesNotHijackQuestionsAboutStudents() {
        assertThat(AssistantEngine.standard().answer("cuantos estudiantes hay", ctx).intent()).isEqualTo("course-counts");
    }
}
