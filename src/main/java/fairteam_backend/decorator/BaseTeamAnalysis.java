package fairteam_backend.decorator;

public class BaseTeamAnalysis implements TeamAnalysis {

    @Override
    public double calculateScore() {
        return 0.0;
    }

    @Override
    public String getExplanation() {
        return "Base team analysis";
    }
}