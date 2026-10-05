package fairteam_backend.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class BalancedLevelStrategy implements TeamFormationStrategy {

    @Override
    public double calculateScore(List<TeamCandidate> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return 0.0;
        }

        double average = candidates.stream()
                .mapToDouble(TeamCandidate::getAcademicScore)
                .average()
                .orElse(0.0);

        double difference = candidates.stream()
                .mapToDouble(candidate ->
                        Math.abs(candidate.getAcademicScore() - average))
                .average()
                .orElse(0.0);

        return Math.max(0.0, 100.0 - (difference * 20.0));
    }
}