package fairteam_backend.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class PerformanceStrategy implements TeamFormationStrategy {

    @Override
    public double calculateScore(List<TeamCandidate> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return 0.0;
        }

        return candidates.stream()
                .mapToDouble(TeamCandidate::getAcademicScore)
                .average()
                .orElse(0.0);
    }
}