package fairteam_backend.application.port.out;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.dto.WorkshopGenerationResponseDTO;
import fairteam_backend.dto.WorkshopRequestDTO;

/** Outbound port used by the application to request a generated workshop. */
public interface AiWorkshopPort {

    /** {@code files} may be empty only for a custom request written from the teacher's prompt. */
    WorkshopGenerationResponseDTO generate(List<MultipartFile> files, WorkshopRequestDTO request);
}
