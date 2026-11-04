package fairteam_backend.insights.domain.assistant;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/** A question already understood: normalized text plus the student, subject and level it mentions. */
public record AssistantQuery(String raw, String text, StudentPerformance student, SubjectAverage subject, Level level) {
    private static final Set<String> STOP = Set.of("de", "del", "la", "las", "los", "el", "en", "y", "a", "para", "con", "que", "por", "un", "una", "mi", "al");

    public static String normalize(String value) {
        String plain = Normalizer.normalize(value == null ? "" : value.toLowerCase(), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return plain.replaceAll("[^a-z0-9ñ\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    public static AssistantQuery parse(String raw, AssistantContext ctx) {
        String text = normalize(raw);
        return new AssistantQuery(raw, text, findStudent(text, ctx), findSubject(text, ctx), findLevel(text));
    }

    /** True when any word of the text starts with one of the given stems (or contains the phrase). */
    public boolean has(String... stems) {
        for (String stem : stems) if (Pattern.compile("(^| )" + Pattern.quote(stem)).matcher(text).find()) return true;
        return false;
    }

    private static StudentPerformance findStudent(String text, AssistantContext ctx) {
        List<String> words = Arrays.stream(text.split(" ")).filter(w -> w.length() >= 3 && !STOP.contains(w)).toList();
        StudentPerformance best = null;
        int bestScore = 0;
        for (StudentPerformance p : ctx.analysis().students()) {
            String[] parts = normalize(p.student().name()).split(" ");
            int score = 0;
            for (int i = 0; i < parts.length; i++) if (parts[i].length() >= 2 && !STOP.contains(parts[i]) && words.contains(parts[i])) score += i == 0 ? 3 : 2;
            if (score > bestScore) { best = p; bestScore = score; }
        }
        return best;
    }

    private static SubjectAverage findSubject(String text, AssistantContext ctx) {
        SubjectAverage best = null;
        int bestScore = 0;
        for (SubjectAverage s : ctx.analysis().subjects()) {
            int score = 0;
            for (String part : normalize(s.name()).split(" ")) {
                if (part.length() < 4 || STOP.contains(part)) continue;
                String stem = part.substring(0, Math.max(4, part.length() - 2));
                if (text.contains(part) || text.contains(stem)) score++;
            }
            if (score > bestScore) { best = s; bestScore = score; }
        }
        return best;
    }

    private static Level findLevel(String text) {
        AssistantQuery probe = new AssistantQuery(text, text, null, null, null);
        if (probe.has("refuerzo", "bajo", "mal", "reprob", "perdiendo", "dificultad")) return Level.LOW;
        if (probe.has("seguimiento", "medio", "intermedio")) return Level.MEDIUM;
        if (probe.has("reto", "avanzad", "alto", "destacad", "profundiz")) return Level.HIGH;
        return null;
    }
}
