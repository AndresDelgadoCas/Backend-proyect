package fairteam_backend.application.port.out;

import org.springframework.web.multipart.MultipartFile;
import java.util.List;

import fairteam_backend.dto.WorkshopGenerationResponseDTO;

/** Outbound port used by the application to request a generated workshop. */
public interface AiWorkshopPort {

    WorkshopGenerationResponseDTO generate(
            List<MultipartFile> files,
            String subject,
            String targetLevel,
            String learningObjective,
            String learningContext,
            String generationType);
}
