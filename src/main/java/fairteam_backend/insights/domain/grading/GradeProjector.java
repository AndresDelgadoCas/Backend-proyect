package fairteam_backend.insights.domain.grading;

import java.util.List;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.PeriodAverage;
import fairteam_backend.insights.domain.StudentPerformance;

/** Domain service: weighted projection of the final grade. */
public class GradeProjector {

    public GradeProjection project(StudentPerformance performance, List<String> observedPeriods, GradingScheme scheme) {
        List<PeriodAverage> points = performance.periods();
        String name = performance.student().name();
        if (points.isEmpty()) return new GradeProjection(performance.student().id(), name, 0, 0, 1, null, ProjectionStatus.NO_DATA, "Sin notas todavía.");
        double earned = 0, done = 0;
        for (int i = 0; i < observedPeriods.size() && i < scheme.periods(); i++) {
            String period = observedPeriods.get(i);
            double weight = scheme.weights().get(i) / 100.0;
            var point = points.stream().filter(p -> p.period().equals(period)).findFirst();
            if (point.isPresent()) { earned += point.get().average() * weight; done += weight; }
        }
        double remaining = Math.max(0, 1 - done);
        double current = done > 0 ? earned / done : 0;
        double passing = scheme.passingGrade();
        if (remaining < 0.001) {
            ProjectionStatus status = Grades.round1(earned) >= passing ? ProjectionStatus.PASSED : ProjectionStatus.IMPOSSIBLE;
            return new GradeProjection(performance.student().id(), name, earned, earned, 0, null, status,
                    (status == ProjectionStatus.PASSED ? "Aprobó con " : "Terminó con ") + Grades.format(earned) + ".");
        }
        double needed = Math.max(0, (passing - earned) / remaining);
        ProjectionStatus status = needed <= 0.0001 ? ProjectionStatus.PASSED : needed <= 3.5 ? ProjectionStatus.POSSIBLE : needed <= 5 ? ProjectionStatus.HARD : ProjectionStatus.IMPOSSIBLE;
        String text = switch (status) {
            case PASSED -> "Ya tiene asegurado el " + Grades.format(passing) + " aunque saque 0 en lo que falta.";
            case IMPOSSIBLE -> "Necesitaría " + Grades.format(needed) + " en lo que falta: ya no alcanza el " + Grades.format(passing) + ".";
            default -> "Necesita " + Grades.format(needed) + " en lo que falta para llegar a " + Grades.format(passing) + ".";
        };
        return new GradeProjection(performance.student().id(), name, earned, current, remaining, needed, status, text);
    }
}
