package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

import fairteam_backend.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllByCourse_IdOrderByNameAsc(Long courseId);
    boolean existsByCourse_Id(Long courseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from ai_evaluations where submission_id in (select id from assignment_submissions where student_id in (select id from students where course_id = :courseId))", nativeQuery = true)
    int deleteEvaluationsOfCourse(@Param("courseId") Long courseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from assignment_submissions where student_id in (select id from students where course_id = :courseId)", nativeQuery = true)
    int deleteSubmissionsOfCourse(@Param("courseId") Long courseId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("delete from Student s where s.course.id = :courseId")
    int deleteAllByCourseId(@Param("courseId") Long courseId);
    Optional<Student> findByEmailIgnoreCase(String email);
}
