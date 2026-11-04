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

/** The assistant's skills, one handler per intent, in the order {@link AssistantEngine} asks them. */
final class Handlers {
    private Handlers() { }

    static ReplyItem item(StudentPerformance p, String value, String note) {
        String tone = !p.hasGrades() ? "none" : switch (p.level()) { case LOW -> "low"; case MEDIUM -> "medium"; case HIGH -> "high"; default -> "none"; };
        return new ReplyItem(p.student().id(), p.student().name(), value, note, tone);
    }

    static String path(StudentPerformance p) {
        return String.join(" → ", p.periods().stream().map(PeriodAverage::average).map(Grades::format).toList());
    }

    static String levelPhrase(Level level) {
        return switch (level) { case LOW -> "de refuerzo"; case MEDIUM -> "para los que están en seguimiento"; case HIGH -> "de reto"; default -> ""; };
    }

    static ReplyAction workshop(String label, String mode, Level level, String topic, StudentPerformance student) {
        return ReplyAction.of("workshop", label, "mode", mode, "level", level.name(), "topic", topic, "studentId", student == null ? null : String.valueOf(student.student().id()));
    }

    static final class CourseCounts implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("cuantos estudiantes", "cuantos alumnos", "cuantos son", "tamano del curso", "cuantas notas")) return Optional.empty();
            CourseSummary s = c.analysis().summary();
            return Optional.of(AssistantReply.say("course-counts", c.analysis().courseName() + " tiene " + Grades.plural(s.students(), "estudiante", "estudiantes")
                    + " y " + Grades.plural(s.gradeCount(), "nota cargada", "notas cargadas") + ": " + s.low() + " en refuerzo, " + s.medium() + " en seguimiento y "
                    + s.high() + " con buen progreso" + (s.ungraded() > 0 ? "; " + s.ungraded() + " sin notas" : "") + "."));
        }
    }

    static final class SubjectExtremes implements IntentHandler {
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
            return Optional.of(easiest ? reply : reply.withActions(workshop("Taller de " + pick.name(), "workshop", Level.LOW, pick.name(), null)));
        }
    }

    static final class PeriodComparison implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("compara", "comparacion", "entre cortes", "por corte", "cada corte")) return Optional.empty();
            List<String> periods = c.analysis().periods();
            if (periods.size() < 2) return Optional.of(AssistantReply.say("period-comparison", "Por ahora hay notas de un solo periodo; necesito dos para comparar."));
            List<String> parts = new java.util.ArrayList<>();
            for (String period : periods) {
                List<Double> values = c.analysis().students().stream().flatMap(p -> p.periods().stream()).filter(x -> x.period().equals(period)).map(PeriodAverage::average).toList();
                if (!values.isEmpty()) parts.add(period + ": " + Grades.format(Grades.average(values)));
            }
            return Optional.of(AssistantReply.say("period-comparison", "Promedio del curso por corte. " + String.join(", ", parts) + ".")
                    .withActions(ReplyAction.of("navigate", "Ver tendencias", "route", "seguimiento", "vista", "tendencia")));
        }
    }

    static final class Fallback implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            return Optional.of(AssistantReply.say("fallback", "No estoy seguro de haberte entendido. Prueba con algo como: “¿quién viene bajando?” o “crea un quiz de funciones”."));
        }
    }
}
