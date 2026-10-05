package fairteam_backend.strategy;

import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class SkillBalancedStrategy implements TeamFormationStrategy {

    @Override
    public double calculateScore(List<TeamCandidate> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            return 0.0;
        }

        return candidates.stream()
                .mapToDouble(TeamCandidate::getSkillScore)
                .average()
                .orElse(0.0);
    }
}