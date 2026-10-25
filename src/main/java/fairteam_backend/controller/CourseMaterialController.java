package fairteam_backend.controller;

import java.util.List;
import java.util.HashSet;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.dto.CourseMaterialDTO;
import fairteam_backend.dto.WorkshopGenerationResponseDTO;
import fairteam_backend.service.AiWorkshopService;
import fairteam_backend.service.CourseMaterialService;
import fairteam_backend.service.StoredMaterialMultipartFile;

@RestController
@RequestMapping("/api/courses/{courseId}/materials")
@CrossOrigin(origins = "*")
public class CourseMaterialController {
    private final CourseMaterialService service;
    private final AiWorkshopService aiWorkshopService;

    public CourseMaterialController(CourseMaterialService service, AiWorkshopService aiWorkshopService) {
        this.service = service;
        this.aiWorkshopService = aiWorkshopService;
    }

    @GetMapping
    public List<CourseMaterialDTO> findAll(@PathVariable Long courseId) {
        return service.findAll(courseId);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CourseMaterialDTO> upload(@PathVariable Long courseId, @RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(CourseMaterialDTO.from(service.upload(courseId, file)));
    }

    @PostMapping(value = "/{materialId}/generate-workshop", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public WorkshopGenerationResponseDTO generateWorkshop(
            @PathVariable Long courseId,
            @PathVariable Long materialId,
            @RequestParam String subject,
            @RequestParam String targetLevel,
            @RequestParam(required = false) String learningObjective,
            @RequestParam(required = false) String learningContext,
            @RequestParam(required = false) String generationType) {
        var material = service.findById(courseId, materialId);
        return aiWorkshopService.generateWorkshop(new StoredMaterialMultipartFile(material), subject, targetLevel,
                learningObjective, learningContext, generationType);
    }

    @PostMapping(value = "/generate-workshop", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public WorkshopGenerationResponseDTO generateWorkshopFromMultipleMaterials(
            @PathVariable Long courseId,
            @RequestParam("materialIds") List<Long> materialIds,
            @RequestParam String subject,
            @RequestParam String targetLevel,
            @RequestParam(required = false) String learningObjective,
            @RequestParam(required = false) String learningContext,
            @RequestParam(required = false) String generationType) {
        if (materialIds == null || materialIds.isEmpty() || materialIds.size() > 6
                || new HashSet<>(materialIds).size() != materialIds.size()) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Selecciona entre 1 y 6 materiales distintos.");
        }
        var files = materialIds.stream()
                .map(id -> new StoredMaterialMultipartFile(service.findById(courseId, id)))
                .map(file -> (MultipartFile) file)
                .toList();
        return aiWorkshopService.generateWorkshop(files, subject, targetLevel,
                learningObjective, learningContext, generationType);
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> delete(@PathVariable Long courseId, @PathVariable Long materialId) {
        service.delete(courseId, materialId);
        return ResponseEntity.noContent().build();
    }
}
