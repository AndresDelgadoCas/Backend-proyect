package fairteam_backend.insights.domain.assistant;

import java.util.Optional;

/** Explains what the assistant can do. */
final class HelpHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("ayuda", "que puedes", "que sabes", "comandos", "como funcionas")) return Optional.empty();
        // "¿Quién necesita ayuda?" is about students, not about the assistant.
        if (q.has("quien", "quienes", "necesita", "necesitan", "estudiante", "estudiantes", "alguien")) return Optional.empty();
        return Optional.of(AssistantReply.say("help", "Puedo decirte cómo va el curso, quién necesita apoyo y quién viene bajando, calcular qué nota necesita alguien para pasar, crear talleres y quizzes, redactar mensajes a tus estudiantes y generar el informe del curso. También te explico cómo hacer cualquier cosa en la app (\"¿cómo pongo porcentajes?\", \"¿dónde subo el material?\") y te llevo al botón exacto. Pregúntame como le preguntarías a un colega."));
    }
}
