package fairteam_backend.factory;

public class TeamRecommendation implements Recommendation {

    private final String message;

    public TeamRecommendation(String message) {
        this.message = message;
    }

    @Override
    public String getType() {
        return "TEAM";
    }

    @Override
    public String getMessage() {
        return message;
    }
}