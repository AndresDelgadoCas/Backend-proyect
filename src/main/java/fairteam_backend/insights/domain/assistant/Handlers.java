package fairteam_backend.insights.domain.assistant;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.PeriodAverage;
import fairteam_backend.insights.domain.StudentPerformance;

/** Small helpers shared by the intent handlers: student cards, trend paths and workshop actions. */
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
}
