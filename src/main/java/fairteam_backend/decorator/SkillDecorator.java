package fairteam_backend.decorator;

public class SkillDecorator extends TeamAnalysisDecorator {

    public SkillDecorator(TeamAnalysis teamAnalysis) {
        super(teamAnalysis);
    }

    @Override
    public double calculateScore() {
        return teamAnalysis.calculateScore() + 35.0;
    }

    @Override
    public String getExplanation() {
        return teamAnalysis.getExplanation()
                + " + skill balance analysis";
    }
}