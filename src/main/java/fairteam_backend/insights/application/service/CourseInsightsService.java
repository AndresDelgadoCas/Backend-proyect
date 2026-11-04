package fairteam_backend.insights.application.service;

import java.time.Clock;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fairteam_backend.insights.application.CourseNotFoundException;
import fairteam_backend.insights.application.StudentNotFoundException;
import fairteam_backend.insights.application.port.in.AskAssistantUseCase;
import fairteam_backend.insights.application.port.in.CourseInsightsUseCase;
import fairteam_backend.insights.application.port.in.DraftFeedbackUseCase;
import fairteam_backend.insights.application.port.out.CourseDataPort;
import fairteam_backend.insights.application.port.out.CourseDataPort.CourseData;
import fairteam_backend.insights.application.port.out.GradingSchemePort;
import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.CourseAnalyzer;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.assistant.AssistantContext;
import fairteam_backend.insights.domain.assistant.AssistantEngine;
import fairteam_backend.insights.domain.assistant.AssistantReply;
import fairteam_backend.insights.domain.briefing.Briefing;
import fairteam_backend.insights.domain.feedback.FeedbackTemplate;
import fairteam_backend.insights.domain.feedback.FeedbackTone;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.GradeProjector;
import fairteam_backend.insights.domain.grading.GradingScheme;

/**
 * Application service: orchestrates the domain (analyzer, projector, briefing, feedback, assistant)
 * through the outbound ports. It has no Spring or JPA imports, so it is unit-testable with fakes.
 */
public class CourseInsightsService implements CourseInsightsUseCase, DraftFeedbackUseCase, AskAssistantUseCase {
    private final CourseDataPort courseData;
    private final GradingSchemePort schemes;
    private final CourseAnalyzer analyzer;
    private final GradeProjector projector;
    private final AssistantEngine assistant;
    private final Clock clock;

    public CourseInsightsService(CourseDataPort courseData, GradingSchemePort schemes, CourseAnalyzer analyzer,
            GradeProjector projector, AssistantEngine assistant, Clock clock) {
        this.courseData = courseData;
        this.schemes = schemes;
        this.analyzer = analyzer;
        this.projector = projector;
        this.assistant = assistant;
        this.clock = clock;
    }

    private record Reading(CourseData data, CourseAnalysis analysis, SchemeView scheme, Map<Long, GradeProjection> projections) { }

    private Reading read(long courseId) {
        CourseData data = courseData.load(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        CourseAnalysis analysis = analyzer.analyze(data.id(), data.name(), data.students(), data.grades());
        var stored = schemes.find(courseId);
        GradingScheme scheme = stored.filter(s -> s.periods() >= analysis.periods().size())
                .orElse(GradingScheme.defaultFor(analysis.periods().size()));
        List<String> periods = new ArrayList<>(analysis.periods());
        while (periods.size() < scheme.periods()) periods.add("Corte " + (periods.size() + 1));
        Map<Long, GradeProjection> projections = new LinkedHashMap<>();
        for (StudentPerformance p : analysis.students()) projections.put(p.student().id(), projector.project(p, analysis.periods(), scheme));
        SchemeView view = new SchemeView(scheme.weights(), scheme.passingGrade(), List.copyOf(periods), analysis.periods(), stored.isPresent());
        return new Reading(data, analysis, view, projections);
    }

    private String briefing(Reading r, String teacher) {
        return Briefing.builder().greet(teacher, LocalTime.now(clock)).state(r.analysis()).support(r.analysis())
                .subject(r.analysis()).trend(r.analysis()).nextStep(r.analysis(), r.data().materials()).build();
    }

    @Override
    public CourseInsights insights(long courseId, String teacherName) {
        Reading r = read(courseId);
        return new CourseInsights(r.analysis(), r.scheme(), List.copyOf(r.projections().values()), briefing(r, teacherName));
    }

    @Override
    public SchemeView updateScheme(long courseId, GradingScheme scheme) {
        courseData.load(courseId).orElseThrow(() -> new CourseNotFoundException(courseId));
        schemes.save(courseId, scheme);
        return read(courseId).scheme();
    }

    @Override
    public Feedback draft(long courseId, long studentId, FeedbackTone tone, String teacherName) {
        Reading r = read(courseId);
        StudentPerformance student = r.analysis().student(studentId).orElseThrow(() -> new StudentNotFoundException(studentId));
        String body = FeedbackTemplate.forTone(tone).compose(student, r.projections().get(studentId), r.data().name(), teacherName);
        return new Feedback(studentId, student.student().name(), student.student().email(), r.data().name() + ": seguimiento de tu proceso", body, tone);
    }

    @Override
    public AssistantReply ask(long courseId, String question, String teacherName) {
        Reading r = read(courseId);
        AssistantContext context = new AssistantContext(teacherName, r.analysis(), r.projections(), r.data().materials(),
                courseData.courses(), briefing(r, teacherName));
        return assistant.answer(question, context);
    }
}
