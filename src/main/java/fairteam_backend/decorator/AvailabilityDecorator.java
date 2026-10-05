package fairteam_backend.decorator;

public class AvailabilityDecorator extends TeamAnalysisDecorator {

    public AvailabilityDecorator(TeamAnalysis teamAnalysis) {
        super(teamAnalysis);
    }

    @Override
    public double calculateScore() {
        return teamAnalysis.calculateScore() + 30.0;
    }

    @Override
    public String getExplanation() {
        return teamAnalysis.getExplanation()
                + " + availability analysis";
    }
}