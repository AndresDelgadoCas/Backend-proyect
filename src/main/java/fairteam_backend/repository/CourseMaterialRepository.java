package fairteam_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

import fairteam_backend.entity.CourseMaterial;

public interface CourseMaterialRepository extends JpaRepository<CourseMaterial, Long> {
    interface Metadata {
        Long getId();
        Long getCourseId();
        String getFileName();
        String getContentType();
        long getFileSize();
        LocalDateTime getCreatedAt();
    }

    @Query("select m.id as id, m.course.id as courseId, m.fileName as fileName, "
            + "m.contentType as contentType, m.fileSize as fileSize, m.createdAt as createdAt "
            + "from CourseMaterial m where m.course.id = :courseId order by m.createdAt desc")
    List<Metadata> findMetadataByCourseId(@Param("courseId") Long courseId);

    Optional<CourseMaterial> findByIdAndCourse_Id(Long id, Long courseId);
}
