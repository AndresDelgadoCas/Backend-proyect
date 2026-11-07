package fairteam_backend.rubrics;

/** Outbound port: asks the AI service for a rubric draft. */
public interface RubricGenerator {
    Rubric.Draft generate(String subject, String activity, int criteriaCount, String teacherPrompt);
}
