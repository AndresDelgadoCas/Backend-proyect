package fairteam_backend.dto;

import java.util.List;

public record WorkshopGenerationResponseDTO(
        String title,
        String subject,
        String targetLevel,
        String summary,
        List<String> learningObjectives,
        List<WorkshopActivityDTO> activities,
        List<AssessmentQuestionDTO> assessment,
        List<String> sourceReferences) {
}
