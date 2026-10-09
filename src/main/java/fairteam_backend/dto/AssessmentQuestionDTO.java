package fairteam_backend.dto;

import java.util.List;

public record AssessmentQuestionDTO(
        String question,
        List<String> options,
        String correctAnswer,
        String explanation) {
}
