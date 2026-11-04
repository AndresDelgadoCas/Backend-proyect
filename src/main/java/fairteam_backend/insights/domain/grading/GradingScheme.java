package fairteam_backend.insights.domain.grading;

import java.util.List;

/** How a course is graded: number of periods and their weights in percent (e.g. 30/30/40). */
public record GradingScheme(List<Integer> weights, double passingGrade) {

    public GradingScheme {
        if (weights == null || weights.isEmpty() || weights.size() > 6) throw new IllegalArgumentException("El esquema debe tener entre 1 y 6 cortes.");
        if (weights.stream().anyMatch(w -> w == null || w < 0 || w > 100)) throw new IllegalArgumentException("Cada peso debe estar entre 0 y 100.");
        if (weights.stream().mapToInt(Integer::intValue).sum() != 100) throw new IllegalArgumentException("Los pesos deben sumar 100%.");
        if (passingGrade <= 0 || passingGrade > 5) throw new IllegalArgumentException("La nota mínima debe estar entre 0 y 5.");
        weights = List.copyOf(weights);
    }

    public int periods() { return weights.size(); }

    public static GradingScheme defaultFor(int observedPeriods) {
        int total = Math.max(3, observedPeriods);
        if (total == 3) return new GradingScheme(List.of(30, 30, 40), 3.0);
        int base = 100 / total;
        List<Integer> weights = new java.util.ArrayList<>(java.util.Collections.nCopies(total, base));
        weights.set(total - 1, 100 - base * (total - 1));
        return new GradingScheme(weights, 3.0);
    }
}
