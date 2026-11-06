package fairteam_backend.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.dto.WorkshopGenerationResponseDTO;
import fairteam_backend.dto.WorkshopRequestDTO;
import fairteam_backend.service.AiWorkshopService;

@RestController
@RequestMapping("/api/ai/workshops")
@CrossOrigin(origins = "*")
public class AiWorkshopController {

    private final AiWorkshopService aiWorkshopService;

    public AiWorkshopController(AiWorkshopService aiWorkshopService) {
        this.aiWorkshopService = aiWorkshopService;
    }

    /** Fields arrive as form parts and bind to {@link WorkshopRequestDTO} by name. */
    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WorkshopGenerationResponseDTO> generate(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @ModelAttribute WorkshopRequestDTO request) {
        List<MultipartFile> files = file == null ? List.of() : List.of(file);
        return ResponseEntity.ok(aiWorkshopService.generateWorkshop(files, request));
    }
}
