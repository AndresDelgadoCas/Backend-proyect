package fairteam_backend.factory;

import org.springframework.stereotype.Component;

@Component
public class WorkshopRecommendationFactory extends RecommendationFactory {

    @Override
    public Recommendation createRecommendation(String message) {
        return new WorkshopRecommendation(message);
    }
}