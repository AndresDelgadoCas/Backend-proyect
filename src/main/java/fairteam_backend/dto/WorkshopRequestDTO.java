package fairteam_backend.dto;

/**
 * What the teacher asks the assistant to create (Parameter Object): it travels unchanged from the controller,
 * through validation, to the AI adapter.
 *
 * @param generationType WORKSHOP, QUIZ or CUSTOM (the teacher describes the workshop in {@code teacherPrompt})
 * @param teacherPrompt  the teacher's own instructions: required for CUSTOM, optional guidance otherwise
 */
public record WorkshopRequestDTO(
        String subject,
        String targetLevel,
        String learningObjective,
        String learningContext,
        String generationType,
        String activityStyles,
        String teacherPrompt) {

    public boolean custom() {
        return "CUSTOM".equalsIgnoreCase(generationType == null ? "" : generationType.trim());
    }
}
