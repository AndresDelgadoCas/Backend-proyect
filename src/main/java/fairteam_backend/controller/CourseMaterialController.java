package fairteam_backend.controller;

import java.util.List;
import java.util.HashSet;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import fairteam_backend.dto.CourseMaterialDTO;
import fairteam_backend.dto.WorkshopGenerationResponseDTO;
import fairteam_backend.dto.WorkshopRequestDTO;
import fairteam_backend.material.MaterialContentProvider;
import fairteam_backend.service.AiWorkshopService;
import fairteam_backend.service.CourseMaterialService;
import fairteam_backend.service.StoredMaterialMultipartFile;

@RestController
@RequestMapping("/api/courses/{courseId}/materials")
@CrossOrigin(origins = "*")
public class CourseMaterialController {
    private final CourseMaterialService service;
    private final AiWorkshopService aiWorkshopService;
    private final MaterialContentProvider contents;

    public CourseMaterialController(CourseMaterialService service, AiWorkshopService aiWorkshopService,
            MaterialContentProvider contents) {
        this.contents = contents;
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
    public WorkshopGenerationResponseDTO generateWorkshop(@PathVariable Long courseId, @PathVariable Long materialId,
            @ModelAttribute WorkshopRequestDTO request) {
        // A proxy: the PDF is read only if the request passes validation and reaches the AI call.
        var material = contents.forCourse(courseId, materialId);
        return aiWorkshopService.generateWorkshop(new StoredMaterialMultipartFile(material), request);
    }

    /**
     * Several materials at once, or none for a custom workshop written from the teacher's instructions
     * ({@code generationType=CUSTOM}).
     */
    @PostMapping(value = "/generate-workshop", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public WorkshopGenerationResponseDTO generateWorkshopFromMultipleMaterials(@PathVariable Long courseId,
            @RequestParam(value = "materialIds", required = false) List<Long> materialIds,
            @ModelAttribute WorkshopRequestDTO request) {
        List<Long> ids = materialIds == null ? List.of() : materialIds;
        if (ids.size() > 6 || new HashSet<>(ids).size() != ids.size() || (ids.isEmpty() && !request.custom())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Selecciona entre 1 y 6 materiales distintos.");
        }
        var files = ids.stream()
                .map(id -> new StoredMaterialMultipartFile(contents.forCourse(courseId, id)))
                .map(file -> (MultipartFile) file)
                .toList();
        return aiWorkshopService.generateWorkshop(files, request);
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> delete(@PathVariable Long courseId, @PathVariable Long materialId) {
        service.delete(courseId, materialId);
        return ResponseEntity.noContent().build();
    }
}
