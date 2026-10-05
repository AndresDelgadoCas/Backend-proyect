package fairteam_backend.decorator;

public class PerformanceDecorator extends TeamAnalysisDecorator {

    public PerformanceDecorator(TeamAnalysis teamAnalysis) {
        super(teamAnalysis);
    }

    @Override
    public double calculateScore() {
        return teamAnalysis.calculateScore() + 35.0;
    }

    @Override
    public String getExplanation() {
        return teamAnalysis.getExplanation()
                + " + academic performance analysis";
    }
}