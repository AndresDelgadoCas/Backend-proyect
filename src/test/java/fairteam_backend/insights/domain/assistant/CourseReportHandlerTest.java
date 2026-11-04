package fairteam_backend.insights.domain.assistant;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CourseReportHandlerTest {
    private final AssistantContext ctx = AssistantFixture.context();

    @Test
    void answersItsIntent() {
        AssistantReply reply = new CourseReportHandler().handle(AssistantFixture.query("genera el informe", ctx), ctx).orElseThrow();
        assertThat(reply.run().kind()).isEqualTo("report");
    }

    @Test
    void ignoresUnrelatedQuestions() {
        assertThat(new CourseReportHandler().handle(AssistantFixture.query("zzz", ctx), ctx)).isEmpty();
    }
}
