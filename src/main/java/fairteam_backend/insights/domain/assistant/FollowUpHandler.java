package fairteam_backend.insights.domain.assistant;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * "Sí", "dale", "hazlo": the teacher accepts what Búho just offered, so Búho does it instead of asking again.
 * Only short replies count; anything longer is a new request.
 */
final class FollowUpHandler implements IntentHandler {
    private static final Pattern YES = Pattern.compile(
            "^(si|sii+|claro|dale|de una|hazlo|haslo|ok|okay|okey|listo|bueno|va|vale|perfecto|adelante|por favor|porfa|sip|aja|hagale|correcto|eso|exacto|sí)"
                    + "( (si|claro|dale|hazlo|por favor|porfa|gracias|de una|crealo|creala|haz eso|eso|perfecto|adelante|buho))*$");

    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (q.text().split(" ").length > 5 || !YES.matcher(q.text()).matches()) return Optional.empty();
        Optional<Turn> last = c.lastBuhoTurn();
        if (last.isEmpty()) return Optional.empty();
        if (last.get().offered().isEmpty()) {
            return Optional.of(AssistantReply.say("follow-up", "¡Listo! ¿Qué hacemos ahora? Puedo crear un taller, una rúbrica o contarte cómo va el curso."));
        }
        ReplyAction action = last.get().offered().get(0);
        if ("workshop".equals(action.kind())) {
            String topic = action.params().get("topic");
            String what = "quiz".equals(action.params().get("mode")) ? "el quiz" : "el taller";
            return Optional.of(AssistantReply.say("follow-up", "Listo, estoy creando " + what + (topic == null ? "" : " sobre " + topic)
                    + ". En unos segundos lo ves en Talleres y queda guardado.").andRun(action.with("auto", "true")));
        }
        return Optional.of(AssistantReply.say("follow-up", "Hecho: " + lowerFirst(action.label()) + ".").andRun(action));
    }

    private static String lowerFirst(String label) {
        return label == null || label.isEmpty() ? "lo que te propuse" : Character.toLowerCase(label.charAt(0)) + label.substring(1);
    }
}
