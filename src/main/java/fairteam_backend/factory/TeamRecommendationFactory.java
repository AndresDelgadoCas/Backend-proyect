package fairteam_backend.factory;

import org.springframework.stereotype.Component;

@Component
public class TeamRecommendationFactory extends RecommendationFactory {

    @Override
    public Recommendation createRecommendation(String message) {
        return new TeamRecommendation(message);
    }
}