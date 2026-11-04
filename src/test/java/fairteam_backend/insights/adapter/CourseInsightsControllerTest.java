package fairteam_backend.insights.adapter;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import fairteam_backend.insights.adapter.in.web.CourseInsightsController;
import fairteam_backend.insights.application.service.CourseInsightsService;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.assistant.AssistantEngine;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

/** HTTP contract of the insights endpoints, with the real service over in-memory ports. */
class CourseInsightsControllerTest {
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        var service = new CourseInsightsService(TestPorts.courseData(), TestPorts.schemes(), new CourseAnalyzer(new TrendWeightedRiskStrategy()),
                new GradeProjector(), AssistantEngine.standard(), Clock.systemUTC());
        Jwt jwt = Jwt.withTokenValue("t").header("alg", "HS256").subject("7").claim("name", "Sebastián Granda")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
        mvc = MockMvcBuilders.standaloneSetup(new CourseInsightsController(service, service, service))
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver()).build();
    }

    @AfterEach
    void clearSecurity() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsTheCourseReading() throws Exception {
        mvc.perform(get("/api/courses/1/insights"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.analysis.summary.students").value(2))
                .andExpect(jsonPath("$.scheme.weights[2]").value(40))
                .andExpect(jsonPath("$.projections.length()").value(2))
                .andExpect(jsonPath("$.analysis.distribution.length()").value(5));
    }

    @Test
    void downloadsTheReportAsCsv() throws Exception {
        mvc.perform(get("/api/courses/1/insights/report.csv"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.header().string("Content-Disposition", org.hamcrest.Matchers.containsString("seguimiento-curso-1.csv")));
    }

    @Test
    void unknownCoursesAreNotFound() throws Exception {
        mvc.perform(get("/api/courses/9/insights")).andExpect(status().isNotFound()).andExpect(jsonPath("$.message").exists());
    }

    @Test
    void validatesTheGradingScheme() throws Exception {
        mvc.perform(put("/api/courses/1/grading-scheme").contentType(MediaType.APPLICATION_JSON).content("{\"weights\":[50,40]}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.message").value("Los pesos deben sumar 100%."));
        mvc.perform(put("/api/courses/1/grading-scheme").contentType(MediaType.APPLICATION_JSON).content("{\"weights\":[50,50]}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.custom").value(true));
    }

    @Test
    void answersAssistantQuestions() throws Exception {
        mvc.perform(post("/api/courses/1/assistant").contentType(MediaType.APPLICATION_JSON).content("{\"question\":\"¿quién viene bajando?\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.intent").value("falling-behind"));
    }

    @Test
    void draftsFeedbackInTheRequestedTone() throws Exception {
        mvc.perform(get("/api/courses/1/students/1/feedback").param("tone", "formal"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tone").value("FORMAL"));
        mvc.perform(get("/api/courses/1/students/1/feedback").param("tone", "motivador"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.tone").value("MOTIVADOR"));
        mvc.perform(get("/api/courses/1/students/77/feedback")).andExpect(status().isNotFound());
    }
}
