package fairteam_backend.dto;

import java.util.List;

public record WorkshopActivityDTO(
        String title,
        String goal,
        String instructions,
        List<String> hints,
        String deliverable,
        int estimatedMinutes) {
}
