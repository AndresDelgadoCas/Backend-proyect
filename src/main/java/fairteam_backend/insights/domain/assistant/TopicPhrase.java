package fairteam_backend.insights.domain.assistant;

import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The topic a teacher names in a request: "un quiz de derivadas para refuerzo" → "derivadas", "un taller de
 * refuerzo sobre la Revolución Francesa" → "la Revolución Francesa" without the article. Words that describe the
 * activity (refuerzo, práctica, nivel…) are skipped, so "taller de refuerzo de fracciones" still finds
 * "fracciones".
 */
final class TopicPhrase {
    private static final Pattern MARK = Pattern.compile("(?iu)(?<![\\p{L}])(sobre|acerca de|del tema|tema|de)\\s+");
    private static final Pattern STOP = Pattern.compile(
            "(?iu)\\s+(en parejas|en grupos?|en equipos?|individual(?:mente)?|en clase|en casa|de \\d+|para|con|en nivel|de nivel|nivel|que|y luego|y despues|y después|donde|usando|enfocad[oa]|dirigid[oa]|basad[oa]|segun|según|a partir)(?![\\p{L}])|[.,;:?!¿¡\"«»]");
    private static final Set<String> SKIP = Set.of("refuerzo", "reto", "seguimiento", "practica", "repaso", "nivel", "taller", "talleres", "quiz",
            "evaluacion", "examen", "actividad", "actividades", "ejercicios", "preguntas", "la clase", "clase", "mi", "mis", "este", "esta",
            "ese", "esa", "lo", "los", "las", "una", "un", "dificultad", "apoyo", "estudiante", "estudiantes", "grupo", "curso");
    private static final Pattern ARTICLE = Pattern.compile("(?iu)^(el|la|los|las|un|una|unos|unas)\\s+");

    private TopicPhrase() { }

    static Optional<String> in(String raw) {
        if (raw == null) return Optional.empty();
        Matcher mark = MARK.matcher(raw);
        int from = 0;
        while (from < raw.length() && mark.find(from)) {
            String rest = raw.substring(mark.end());
            Matcher stop = STOP.matcher(rest);
            String candidate = (stop.find() ? rest.substring(0, stop.start()) : rest).trim();
            candidate = ARTICLE.matcher(candidate).replaceFirst("").trim();
            String first = AssistantQuery.normalize(candidate.split("\\s+")[0]);
            if (candidate.length() >= 3 && !SKIP.contains(first) && !SKIP.contains(AssistantQuery.normalize(candidate))) {
                return Optional.of(candidate.length() > 80 ? candidate.substring(0, 80).trim() : candidate);
            }
            from = mark.end();
        }
        return Optional.empty();
    }
}
