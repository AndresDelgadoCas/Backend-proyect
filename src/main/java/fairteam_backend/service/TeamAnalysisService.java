package fairteam_backend.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fairteam_backend.decorator.AvailabilityDecorator;
import fairteam_backend.decorator.BaseTeamAnalysis;
import fairteam_backend.decorator.PerformanceDecorator;
import fairteam_backend.decorator.SkillDecorator;
import fairteam_backend.decorator.TeamAnalysis;
import fairteam_backend.strategy.TeamCandidate;
import fairteam_backend.strategy.TeamFormationStrategy;

@Service
public class TeamAnalysisService {

    /**
     * Calculates a team score using the selected Strategy
     * and enriches the analysis using Decorators.
     */
    public TeamAnalysisResult analyzeTeam(
            List<TeamCandidate> candidates,
            TeamFormationStrategy strategy) {

        if (candidates == null || candidates.isEmpty()) {
            return new TeamAnalysisResult(
                    0.0,
                    "No candidates available for team analysis."
            );
        }

        // Strategy calculates the initial score.
        double strategyScore = strategy.calculateScore(candidates);

        // Base analysis uses the Strategy result.
        TeamAnalysis analysis = new BaseTeamAnalysis() {
            @Override
            public double calculateScore() {
                return strategyScore;
            }

            @Override
            public String getExplanation() {
                return "Team evaluated using "
                        + strategy.getClass().getSimpleName();
            }
        };

        // Decorators add additional evaluation criteria.
        analysis = new PerformanceDecorator(analysis);
        analysis = new SkillDecorator(analysis);
        analysis = new AvailabilityDecorator(analysis);

        // Keep the final score within 0-100.
        double finalScore = Math.min(100.0, analysis.calculateScore());

        return new TeamAnalysisResult(
                finalScore,
                analysis.getExplanation()
        );
    }

    public static class TeamAnalysisResult {

        private final double score;
        private final String explanation;

        public TeamAnalysisResult(double score, String explanation) {
            this.score = score;
            this.explanation = explanation;
        }

        public double getScore() {
            return score;
        }

        public String getExplanation() {
            return explanation;
        }
    }
}