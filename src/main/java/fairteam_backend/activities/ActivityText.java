package fairteam_backend.activities;

import tools.jackson.databind.JsonNode;

/** Turns a saved activity into plain text the AI can read (to build a rubric that matches it). */
public final class ActivityText {
    private static final int MAX = 6000;

    private ActivityText() { }

    public static String of(Activity.View activity) {
        JsonNode c = activity.content();
        StringBuilder text = new StringBuilder();
        text.append("Actividad: ").append(activity.title()).append('\n');
        if (activity.topic() != null) text.append("Tema: ").append(activity.topic()).append('\n');
        append(text, "Resumen: ", c.path("summary").asString(""));
        JsonNode objectives = c.path("learningObjectives");
        if (objectives.isArray() && !objectives.isEmpty()) {
            text.append("Objetivos:");
            objectives.forEach(o -> text.append(" ").append(o.asString("")).append(';'));
            text.append('\n');
        }
        int n = 1;
        for (JsonNode a : c.path("activities")) {
            text.append(n++).append(". ").append(a.path("title").asString(""));
            String deliverable = a.path("deliverable").asString("");
            if (!deliverable.isBlank()) text.append(" — Entrega: ").append(deliverable);
            text.append('\n').append(cut(a.path("instructions").asString(""), 700)).append('\n');
        }
        int q = 1;
        for (JsonNode question : c.path("assessment")) {
            text.append("Pregunta ").append(q++).append(": ").append(question.path("question").asString("")).append('\n');
        }
        return cut(text.toString(), MAX);
    }

    private static void append(StringBuilder text, String label, String value) {
        if (!value.isBlank()) text.append(label).append(value).append('\n');
    }

    private static String cut(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max);
    }
}
