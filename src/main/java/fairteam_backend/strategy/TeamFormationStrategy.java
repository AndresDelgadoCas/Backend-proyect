package fairteam_backend.strategy;

import java.util.List;

public interface TeamFormationStrategy {

    double calculateScore(List<TeamCandidate> candidates);
}