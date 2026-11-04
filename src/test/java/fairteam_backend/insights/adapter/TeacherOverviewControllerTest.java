package fairteam_backend.insights.adapter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import fairteam_backend.insights.adapter.in.web.TeacherOverviewController;
import fairteam_backend.insights.application.service.CourseInsightsService;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.assistant.AssistantEngine;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

class TeacherOverviewControllerTest {
    @Test
    void listsCoursesWithTheirSummary() throws Exception {
        var service = new CourseInsightsService(TestPorts.courseData(), TestPorts.schemes(), new CourseAnalyzer(new TrendWeightedRiskStrategy()),
                new GradeProjector(), AssistantEngine.standard(), Clock.systemUTC());
        MockMvcBuilders.standaloneSetup(new TeacherOverviewController(service)).build()
                .perform(get("/api/insights/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Física"))
                .andExpect(jsonPath("$[0].students").value(2))
                .andExpect(jsonPath("$[0].fallingBehind").value(1));
    }
}
