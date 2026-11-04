package fairteam_backend.insights.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import fairteam_backend.insights.application.port.out.CourseDataPort;
import fairteam_backend.insights.application.port.out.GradingSchemePort;
import fairteam_backend.insights.application.service.CourseInsightsService;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.assistant.AssistantEngine;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.risk.RiskScoringStrategy;
import fairteam_backend.insights.domain.risk.TrendWeightedRiskStrategy;

/** Composition root of the insights module: the only place that knows which implementations are used. */
@Configuration
public class InsightsConfiguration {

    @Bean
    RiskScoringStrategy riskScoringStrategy() { return new TrendWeightedRiskStrategy(); }

    @Bean
    CourseInsightsService courseInsightsService(CourseDataPort courseData, GradingSchemePort schemes, RiskScoringStrategy risk) {
        return new CourseInsightsService(courseData, schemes, new CourseAnalyzer(risk), new GradeProjector(),
                AssistantEngine.standard(), Clock.system(ZoneId.of("America/Bogota")));
    }
}
