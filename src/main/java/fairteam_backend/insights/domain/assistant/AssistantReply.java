package fairteam_backend.insights.domain.assistant;

import java.util.List;

/** What the assistant says, the students it points at, what it offers to do and what it does right away. */
public record AssistantReply(String text, String intent, List<ReplyItem> items, List<ReplyAction> actions, ReplyAction run) {
    public static AssistantReply say(String intent, String text) { return new AssistantReply(text, intent, List.of(), List.of(), null); }
    public AssistantReply withItems(List<ReplyItem> list) { return new AssistantReply(text, intent, List.copyOf(list), actions, run); }
    public AssistantReply withActions(ReplyAction... list) { return new AssistantReply(text, intent, items, List.of(list), run); }
    public AssistantReply andRun(ReplyAction action) { return new AssistantReply(text, intent, items, actions, action); }
}
