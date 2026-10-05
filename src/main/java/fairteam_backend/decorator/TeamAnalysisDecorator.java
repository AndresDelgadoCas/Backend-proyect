package fairteam_backend.decorator;

public abstract class TeamAnalysisDecorator implements TeamAnalysis {

    protected final TeamAnalysis teamAnalysis;

    protected TeamAnalysisDecorator(TeamAnalysis teamAnalysis) {
        this.teamAnalysis = teamAnalysis;
    }
}