package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.Team;
import fairteam_backend.factory.Recommendation;
import fairteam_backend.repository.TeamRepository;

@Service
public class TeamService {

    private final TeamRepository teamRepository;
    private final RecommendationService recommendationService;

    public TeamService(
            TeamRepository teamRepository,
            RecommendationService recommendationService) {

        this.teamRepository = teamRepository;
        this.recommendationService = recommendationService;
    }

    public List<Team> findAll() {
        return teamRepository.findAll();
    }

    public Optional<Team> findById(Long id) {
        return teamRepository.findById(id);
    }

    public Team save(Team team) {
        return teamRepository.save(team);
    }

    public Team update(Long id, Team team) {
        return teamRepository.findById(id)
                .map(existing -> {
                    existing.setProject(team.getProject());
                    existing.setName(team.getName());
                    existing.setFormationStrategy(team.getFormationStrategy());
                    existing.setScore(team.getScore());
                    existing.setExplanation(team.getExplanation());
                    return teamRepository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException(
                                "Team not found with id: " + id));
    }

    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException(
                    "Team not found with id: " + id);
        }

        teamRepository.deleteById(id);
    }

    public Recommendation createTeamRecommendation(String message) {
        return recommendationService.createTeamRecommendation(message);
    }
}