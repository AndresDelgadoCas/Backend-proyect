package fairteam_backend.insights.domain.assistant;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.SubjectAverage;

/**
 * "¿Cómo pongo porcentajes?", "¿dónde subo el material?": answers from the {@link AppManual} with the exact
 * steps, a button that opens the page and rings the control, and "Hazlo por mí" when Búho can do it itself.
 */
final class HowToHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!AppManual.asksHow(q)) return Optional.empty();
        return AppManual.find(q).map(entry -> reply(entry, q, c));
    }

    static AssistantReply reply(AppManual.HowTo entry, AssistantQuery q, AssistantContext c) {
        List<ReplyAction> actions = new ArrayList<>();
        ReplyAction doIt = entry.doIt();
        if (doIt != null && "workshop".equals(doIt.kind())) {
            String topic = q == null ? null : q.subject() != null ? q.subject().name() : TopicPhrase.in(q.raw()).orElse(null);
            if (topic == null) topic = c.analysis().weakestSubject().map(SubjectAverage::name).orElse(null);
            doIt = doIt.with("topic", topic);
        }
        if (doIt != null && !"guide".equals(doIt.kind())) actions.add(doIt);
        actions.add(ReplyAction.of("navigate", "Llévame", "route", entry.route(), "vista", entry.vista(), "resaltar", entry.spot()));
        AssistantReply reply = AssistantReply.say("how-to", entry.answer()).withActions(actions.toArray(ReplyAction[]::new));
        return doIt != null && "guide".equals(doIt.kind()) ? reply.andRun(doIt) : reply;
    }
}
