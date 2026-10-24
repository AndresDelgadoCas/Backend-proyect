package fairteam_backend.application.port.out;

import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.dto.WorkshopGenerationResponseDTO;

/** Outbound port used by the application to request a generated workshop. */
public interface AiWorkshopPort {

    WorkshopGenerationResponseDTO generate(
            MultipartFile file,
            String subject,
            String targetLevel,
            String learningObjective);
}
