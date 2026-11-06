package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.attendance.AttendanceStats;

/** Answers questions about attendance: who misses class and how that combines with grades. */
final class AttendanceHandler implements IntentHandler {
    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        if (!q.has("asistencia", "falta", "faltado", "faltan a clase", "ausencia", "ausente", "inasistencia", "no viene", "no han venido")) {
            return Optional.empty();
        }
        // "faltan notas" belongs to MissingGradesHandler, which runs earlier in the chain.
        if (c.attendance().isEmpty()) {
            return Optional.of(AssistantReply.say("attendance", "Aún no has tomado asistencia en este curso. Pásala desde Estudiantes y notas y te aviso quién falta seguido.")
                    .withActions(ReplyAction.of("navigate", "Tomar asistencia", "route", "estudiantes", "vista", "asistencia")));
        }
        List<StudentPerformance> flagged = c.analysis().students().stream()
                .filter(p -> c.attendance().containsKey(p.student().id()) && c.attendance().get(p.student().id()).alert())
                .sorted(Comparator.comparingDouble(p -> c.attendance().get(p.student().id()).rate())).toList();
        if (flagged.isEmpty()) {
            double average = c.attendance().values().stream().mapToDouble(AttendanceStats::rate).average().orElse(1.0);
            return Optional.of(AssistantReply.say("attendance", "La asistencia va bien: el grupo asiste en promedio al "
                    + Math.round(average * 100) + " % de las clases y nadie baja del 80 %."));
        }
        long withLowGrades = flagged.stream().filter(p -> p.hasGrades() && Grades.round1(p.average()) < Grades.PASSING).count();
        String text = Grades.plural(flagged.size(), "estudiante asiste", "estudiantes asisten") + " a menos del 80 % de las clases"
                + (withLowGrades > 0 ? "; " + Grades.plural(withLowGrades, "de ellos también está", "de ellos también están") + " por debajo de " + Grades.format(Grades.PASSING) + ". Empieza por ahí." : ".");
        return Optional.of(AssistantReply.say("attendance", text)
                .withItems(flagged.stream().limit(8).map(p -> {
                    AttendanceStats a = c.attendance().get(p.student().id());
                    return new ReplyItem(p.student().id(), p.student().name(), a.percent() + " %",
                            Grades.plural(a.absent(), "falta", "faltas") + (p.hasGrades() ? " · promedio " + Grades.format(p.average()) : ""),
                            p.hasGrades() && Grades.round1(p.average()) < Grades.PASSING ? "low" : "medium");
                }).toList())
                .withActions(ReplyAction.of("navigate", "Ver asistencia", "route", "estudiantes", "vista", "asistencia")));
    }
}
