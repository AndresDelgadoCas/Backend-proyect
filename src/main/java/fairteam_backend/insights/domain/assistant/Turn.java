package fairteam_backend.insights.domain.assistant;

import java.util.List;

/**
 * One earlier message of the conversation, sent back by the browser so Búho remembers it: who said it, the
 * text, what Búho offered (its buttons) and what it already ran.
 */
public record Turn(String from, String text, List<ReplyAction> offered, ReplyAction ran) {
    public Turn {
        from = "buho".equals(from) ? "buho" : "teacher";
        text = text == null ? "" : text.length() > 600 ? text.substring(0, 600) : text;
        offered = offered == null ? List.of() : List.copyOf(offered);
    }

    public static Turn teacher(String text) { return new Turn("teacher", text, List.of(), null); }

    public static Turn buho(String text, List<ReplyAction> offered, ReplyAction ran) { return new Turn("buho", text, offered, ran); }

    public boolean fromBuho() { return "buho".equals(from); }
}
