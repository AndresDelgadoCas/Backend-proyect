package fairteam_backend.insights.domain.risk;

import fairteam_backend.insights.domain.Direction;

/**
 * Default strategy: distance below 4,2 drives the score; falling between periods adds up to 30 points,
 * improving removes 10. Students without grades get a neutral 35 so they are not forgotten.
 */
public class TrendWeightedRiskStrategy implements RiskScoringStrategy {
    @Override
    public int score(Double average, Direction direction, double delta) {
        if (average == null) return 35;
        double risk = Math.max(0, Math.min(100, (4.2 - average) * 45));
        if (direction == Direction.DOWN) risk += Math.min(30, Math.abs(delta) * 30);
        if (direction == Direction.UP) risk -= 10;
        return (int) Math.round(Math.max(0, Math.min(100, risk)));
    }
}
