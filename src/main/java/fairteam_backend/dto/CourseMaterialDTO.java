package fairteam_backend.dto;

import java.time.LocalDateTime;

import fairteam_backend.entity.CourseMaterial;

public record CourseMaterialDTO(Long id, Long courseId, String fileName, String contentType,
        long fileSize, LocalDateTime createdAt) {
    public static CourseMaterialDTO from(CourseMaterial material) {
        return new CourseMaterialDTO(material.getId(), material.getCourseId(), material.getFileName(),
                material.getContentType(), material.getFileSize(), material.getCreatedAt());
    }
}
