package fairteam_backend.service;

import org.springframework.stereotype.Service;

import fairteam_backend.factory.Recommendation;
import fairteam_backend.factory.RecommendationFactory;
import fairteam_backend.factory.TeamRecommendationFactory;
import fairteam_backend.factory.WorkshopRecommendationFactory;

@Service
public class RecommendationService {

    private final TeamRecommendationFactory teamRecommendationFactory;
    private final WorkshopRecommendationFactory workshopRecommendationFactory;

    public RecommendationService(
            TeamRecommendationFactory teamRecommendationFactory,
            WorkshopRecommendationFactory workshopRecommendationFactory) {

        this.teamRecommendationFactory = teamRecommendationFactory;
        this.workshopRecommendationFactory = workshopRecommendationFactory;
    }

    public Recommendation createTeamRecommendation(String message) {
        return createRecommendation(teamRecommendationFactory, message);
    }

    public Recommendation createWorkshopRecommendation(String message) {
        return createRecommendation(workshopRecommendationFactory, message);
    }

    private Recommendation createRecommendation(
            RecommendationFactory factory,
            String message) {

        return factory.createRecommendation(message);
    }
}