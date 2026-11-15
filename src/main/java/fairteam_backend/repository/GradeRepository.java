package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

import fairteam_backend.entity.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findAllByCourse_Id(Long courseId);
    boolean existsByCourse_Id(Long courseId);

    @Query("select distinct g.subject.id from Grade g where g.course.id = :courseId")
    List<Long> subjectIdsOfCourse(@Param("courseId") Long courseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Grade g where g.course.id = :courseId")
    int deleteAllByCourseId(@Param("courseId") Long courseId);

    /** Subjects are shared per teacher; once a course is gone only the ones nothing else uses are removed. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from subjects s where s.id in (:ids) and not exists (select 1 from grades g where g.subject_id = s.id) "
            + "and not exists (select 1 from assignments a where a.subject_id = s.id)", nativeQuery = true)
    int deleteUnusedSubjects(@Param("ids") java.util.Collection<Long> ids);
    java.util.Optional<Grade> findByStudent_IdAndSubject_IdAndPeriod(Long studentId, Long subjectId, String period);
}
