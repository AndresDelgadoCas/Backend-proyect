package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.CourseSummary;
import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.PeriodAverage;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/** Names the hardest or easiest subject. */
final class SubjectExtremesHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("tema", "temas", "materia")) return Optional.empty();
        boolean easiest = q.has("facil", "mejor", "mas alto");
        boolean hardest = q.has("dificil", "cuesta", "peor", "mas bajo", "flojo");
        List<SubjectAverage> subjects = c.analysis().subjects();
        if ((!easiest && !hardest) || subjects.isEmpty()) return Optional.empty();
        SubjectAverage pick = easiest ? subjects.get(subjects.size() - 1) : subjects.get(0);
        String text = (easiest ? "El tema con mejor promedio es " : "El tema que más cuesta es ") + pick.name() + ", con " + Grades.format(pick.average())
                + (pick.studentsBelowPassing() > 0 ? "; " + Grades.plural(pick.studentsBelowPassing(), "estudiante está", "estudiantes están") + " por debajo de 3,0." : ".");
        AssistantReply reply = AssistantReply.say("subject-extremes", text);
        return Optional.of(easiest ? reply : reply.withActions(Handlers.workshop("Taller de " + pick.name(), "workshop", Level.LOW, pick.name(), null)));
    }
}
