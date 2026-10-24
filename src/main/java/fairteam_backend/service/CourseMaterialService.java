package fairteam_backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.entity.Course;
import fairteam_backend.entity.CourseMaterial;
import fairteam_backend.dto.CourseMaterialDTO;
import fairteam_backend.repository.CourseMaterialRepository;
import fairteam_backend.repository.CourseRepository;

@Service
public class CourseMaterialService {
    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private final CourseMaterialRepository materials;
    private final CourseRepository courses;

    public CourseMaterialService(CourseMaterialRepository materials, CourseRepository courses) {
        this.materials = materials;
        this.courses = courses;
    }

    @Transactional(readOnly = true)
    public List<CourseMaterialDTO> findAll(Long courseId) {
        requireCourse(courseId);
        return materials.findMetadataByCourseId(courseId).stream()
                .map(item -> new CourseMaterialDTO(item.getId(), item.getCourseId(), item.getFileName(),
                        item.getContentType(), item.getFileSize(), item.getCreatedAt()))
                .toList();
    }

    @Transactional
    public CourseMaterial upload(Long courseId, MultipartFile file) {
        requireCourse(courseId);
        validate(file);
        try {
            CourseMaterial material = new CourseMaterial();
            material.setCourse(courses.getReferenceById(courseId));
            material.setFileName(file.getOriginalFilename().trim());
            material.setContentType(file.getContentType() == null ? "application/octet-stream" : file.getContentType());
            material.setFileSize(file.getSize());
            material.setContent(file.getBytes());
            return materials.save(material);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo.", exception);
        }
    }

    @Transactional(readOnly = true)
    public CourseMaterial findById(Long courseId, Long materialId) {
        requireCourse(courseId);
        return materials.findByIdAndCourse_Id(materialId, courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el material en este curso."));
    }

    @Transactional
    public void delete(Long courseId, Long materialId) {
        materials.delete(findById(courseId, materialId));
    }

    private void requireCourse(Long courseId) {
        if (courseId == null || !courses.existsById(courseId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
        }
    }

    private static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un archivo para subir.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo supera el límite de 10 MB.");
        }
        String filename = file.getOriginalFilename();
        String lower = filename == null ? "" : filename.toLowerCase(Locale.ROOT);
        if (!(lower.endsWith(".pdf") || lower.endsWith(".docx") || lower.endsWith(".txt"))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usa un archivo PDF, DOCX o TXT.");
        }
    }
}
