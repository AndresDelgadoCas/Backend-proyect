package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

import fairteam_backend.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
    List<Student> findAllByCourse_IdOrderByNameAsc(Long courseId);
    boolean existsByCourse_Id(Long courseId);
    Optional<Student> findByEmailIgnoreCase(String email);
}
