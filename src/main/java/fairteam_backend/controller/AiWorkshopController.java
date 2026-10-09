package fairteam_backend.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.dto.WorkshopGenerationResponseDTO;
import fairteam_backend.service.AiWorkshopService;

@RestController
@RequestMapping("/api/ai/workshops")
@CrossOrigin(origins = "*")
public class AiWorkshopController {

    private final AiWorkshopService aiWorkshopService;

    public AiWorkshopController(AiWorkshopService aiWorkshopService) {
        this.aiWorkshopService = aiWorkshopService;
    }

    @PostMapping(value = "/generate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<WorkshopGenerationResponseDTO> generate(
            @RequestParam("file") MultipartFile file,
            @RequestParam("subject") String subject,
            @RequestParam("targetLevel") String targetLevel,
            @RequestParam(value = "learningObjective", required = false) String learningObjective) {

        return ResponseEntity.ok(aiWorkshopService.generateWorkshop(
                file,
                subject,
                targetLevel,
                learningObjective));
    }
}
