package fairteam_backend.factory;

public class WorkshopRecommendation implements Recommendation {

    private final String message;

    public WorkshopRecommendation(String message) {
        this.message = message;
    }

    @Override
    public String getType() {
        return "WORKSHOP";
    }

    @Override
    public String getMessage() {
        return message;
    }
}