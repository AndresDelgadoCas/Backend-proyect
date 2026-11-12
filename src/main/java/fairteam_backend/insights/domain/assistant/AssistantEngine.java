package fairteam_backend.insights.domain.assistant;

import java.util.List;

/** Runs the chain of handlers in order; the last one always answers. */
public class AssistantEngine {
    private final List<IntentHandler> chain;

    public AssistantEngine(List<IntentHandler> chain) { this.chain = List.copyOf(chain); }

    public static AssistantEngine standard() {
        return new AssistantEngine(List.of(
                new FollowUpHandler(), new SwitchCourseHandler(), new SmallTalkHandler(), new HelpHandler(), new HowToHandler(), new NoCourseDataHandler(), new DailyBriefingHandler(),
                new CreateRubricHandler(), new CreateActivityHandler(), new DraftMessageHandler(), new CourseReportHandler(), new NeedsGradesHandler(), new ExplainRiskHandler(), new TeacherAdviceHandler(),
                new CourseTopicGate(new PassingGradeHandler()), new CourseTopicGate(new MissingGradesHandler()), new CourseTopicGate(new AttendanceHandler()), new CourseTopicGate(new CourseCountsHandler()), new CourseTopicGate(new WorstStudentsHandler()), new CourseTopicGate(new SubjectExtremesHandler()), new CourseTopicGate(new PeriodComparisonHandler()), new CourseTopicGate(new FallingBehindHandler()), new CourseTopicGate(new ImprovingHandler()), new CourseTopicGate(new BestStudentsHandler()), new CourseTopicGate(new StudentStatusHandler()), new CourseTopicGate(new AveragesHandler()), new CourseTopicGate(new WhoNeedsHelpHandler()), new NavigationHandler(),
                new FallbackHandler()));
    }

    public AssistantReply answer(String question, AssistantContext context) {
        AssistantQuery query = AssistantQuery.parse(question, context);
        if (query.text().isEmpty()) return AssistantReply.say("empty", "¿En qué te ayudo?");
        for (IntentHandler handler : chain) {
            var reply = handler.handle(query, context);
            if (reply.isPresent()) return reply.get();
        }
        return AssistantReply.say("fallback", "No estoy seguro de haberte entendido.");
    }
}
