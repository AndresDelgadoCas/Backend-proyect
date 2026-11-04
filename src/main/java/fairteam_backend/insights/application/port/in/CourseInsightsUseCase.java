package fairteam_backend.insights.application.port.in;

import java.util.List;

import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.GradingScheme;

/** Inbound port: the full reading of a course, its projections and the spoken briefing. */
public interface CourseInsightsUseCase {
    record SchemeView(List<Integer> weights, double passingGrade, List<String> periods, List<String> observedPeriods, boolean custom) { }

    record CourseInsights(CourseAnalysis analysis, SchemeView scheme, List<GradeProjection> projections, String briefing) { }

    CourseInsights insights(long courseId, String teacherName);

    SchemeView updateScheme(long courseId, GradingScheme scheme);
}
