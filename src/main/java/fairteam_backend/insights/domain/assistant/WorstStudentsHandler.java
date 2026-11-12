package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/**
 * "¿Qué estudiante va peor en esta materia?": the lowest averages of the course (or of the topic named), with
 * the student first. "Materia" here is the course, so the question is about people, not about topics.
 */
final class WorstStudentsHandler implements IntentHandler {
    static boolean aboutStudents(AssistantQuery q) {
        return q.has("estud", "alumn", "quien", "quienes") && !q.has("tema", "temas");
    }

    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!aboutStudents(q) || !q.has("peor", "peores", "mas bajo", "mas baja", "mas mal", "va mal", "van mal", "menor promedio", "mas flojo", "ultimo", "ultimos")) return Optional.empty();
        SubjectAverage subject = q.subject();
        record Row(StudentPerformance p, double average) { }
        List<Row> rows = c.analysis().students().stream().filter(StudentPerformance::hasGrades)
                .map(p -> subject == null ? new Row(p, p.average())
                        : p.subjects().stream().filter(s -> s.subjectId() == subject.subjectId()).findFirst().map(s -> new Row(p, s.average())).orElse(null))
                .filter(r -> r != null).sorted(Comparator.comparingDouble(Row::average)).limit(5).toList();
        if (rows.isEmpty()) return Optional.empty();
        StudentPerformance worst = rows.get(0).p();
        String where = subject == null ? "en " + c.analysis().courseName() : "en " + subject.name();
        String weakest = subject == null && worst.weakest() != null ? " Lo que más le cuesta es " + worst.weakest().name() + "." : "";
        String text = worst.student().name() + " tiene el promedio más bajo " + where + ": " + Grades.format(rows.get(0).average()) + "." + weakest
                + (rows.size() > 1 ? " Te dejo los " + rows.size() + " promedios más bajos." : "");
        String topic = subject != null ? subject.name() : worst.weakest() != null ? worst.weakest().name() : null;
        return Optional.of(AssistantReply.say("worst-students", text)
                .withItems(rows.stream().map(r -> Handlers.item(r.p(), Grades.format(r.average()), r.p().level().label())).toList())
                .withActions(Handlers.workshop("Refuerzo para " + Grades.firstName(worst.student().name()), "workshop", worst.level(), topic, worst),
                        ReplyAction.of("review", "Ver su ficha", "studentId", String.valueOf(worst.student().id()))));
    }
}
