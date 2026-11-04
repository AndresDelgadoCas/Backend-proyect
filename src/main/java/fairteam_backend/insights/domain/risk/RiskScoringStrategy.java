package fairteam_backend.insights.domain.risk;

import fairteam_backend.insights.domain.Direction;

/** Strategy: how a student's average and trend become a 0–100 risk score. */
public interface RiskScoringStrategy {
    int score(Double average, Direction direction, double delta);
}
