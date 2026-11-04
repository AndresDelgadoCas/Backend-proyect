package fairteam_backend.insights.domain.assistant;

import java.util.List;

/** Runs the chain of handlers in order; the last one always answers. */
public class AssistantEngine {
    private final List<IntentHandler> chain;

    public AssistantEngine(List<IntentHandler> chain) { this.chain = List.copyOf(chain); }

    public static AssistantEngine standard() {
        return new AssistantEngine(List.of(
                new Handlers.SwitchCourse(), new Handlers.Help(), new Handlers.NoCourseData(), new Handlers.DailyBriefing(),
                new Handlers.CreateActivity(), new Handlers.DraftMessage(), new Handlers.CourseReport(), new Handlers.NeedsGrades(),
                new Handlers.PassingGrade(), new Handlers.MissingGrades(), new Handlers.CourseCounts(), new Handlers.SubjectExtremes(), new Handlers.PeriodComparison(), new Handlers.FallingBehind(), new Handlers.Improving(), new Handlers.BestStudents(),
                new Handlers.StudentStatus(), new Handlers.Averages(), new Handlers.WhoNeedsHelp(), new Handlers.Navigation(),
                new Handlers.Fallback()));
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
