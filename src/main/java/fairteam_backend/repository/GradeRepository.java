package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

import fairteam_backend.entity.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findAllByCourse_Id(Long courseId);
    boolean existsByCourse_Id(Long courseId);
    java.util.Optional<Grade> findByStudent_IdAndSubject_IdAndPeriod(Long studentId, Long subjectId, String period);
}
