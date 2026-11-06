package fairteam_backend.insights.domain.assistant;

import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.CourseSummary;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.SubjectAverage;

/** Answers course or subject averages. */
final class AveragesHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("promedio", "media", "nota del curso", "como van")) return Optional.empty();
        SubjectAverage subject = q.subject();
        if (subject != null) {
            return Optional.of(AssistantReply.say("average", "En " + subject.name() + " el promedio es " + Grades.format(subject.average())
                    + (subject.studentsBelowPassing() > 0 ? " y " + Grades.plural(subject.studentsBelowPassing(), "estudiante está", "estudiantes están") + " por debajo de " + Grades.format(Grades.PASSING) : "") + ".")
                    .withActions(Handlers.workshop("Taller de " + subject.name(), "workshop", Level.LOW, subject.name(), null)));
        }
        CourseSummary s = c.analysis().summary();
        List<SubjectAverage> subjects = c.analysis().subjects();
        String extra = subjects.isEmpty() ? "" : " El tema más bajo es " + subjects.get(0).name() + " (" + Grades.format(subjects.get(0).average()) + ") y el más alto "
                + subjects.get(subjects.size() - 1).name() + " (" + Grades.format(subjects.get(subjects.size() - 1).average()) + ").";
        return Optional.of(AssistantReply.say("average", "El promedio de " + c.analysis().courseName() + " es " + Grades.format(s.average()) + " con " + Grades.plural(s.gradeCount(), "nota", "notas") + "." + extra));
    }
}
