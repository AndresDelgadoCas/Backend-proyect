package fairteam_backend.insights.domain.risk;

import fairteam_backend.insights.domain.Direction;

/** Alternative strategy: risk depends only on the average (useful when periods are not comparable). */
public class GradeOnlyRiskStrategy implements RiskScoringStrategy {
    @Override
    public int score(Double average, Direction direction, double delta) {
        if (average == null) return 35;
        return (int) Math.round(Math.max(0, Math.min(100, (4.2 - average) * 45)));
    }
}
