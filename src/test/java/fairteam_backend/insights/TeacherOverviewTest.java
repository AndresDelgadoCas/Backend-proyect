package fairteam_backend.insights;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;

import org.junit.jupiter.api.Test;

import fairteam_backend.insights.application.service.CourseInsightsService;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.assistant.AssistantEngine;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

class TeacherOverviewTest {
    @Test
    void listsEveryCourseTheTeacherCanSee() {
        var service = new CourseInsightsService(InsightsFixtures.courseData(), InsightsFixtures.schemes(),
                new CourseAnalyzer(new TrendWeightedRiskStrategy()), new GradeProjector(), AssistantEngine.standard(), Clock.systemUTC());
        var rows = service.overview();
        // the fixture only has data for course 1; course 2 is listed but cannot be loaded
        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).name()).isEqualTo("Programación I");
        assertThat(rows.get(0).fallingBehind()).isEqualTo(2);
        assertThat(rows.get(0).weakestSubject()).isEqualTo("Ciclos y condicionales");
    }
}
