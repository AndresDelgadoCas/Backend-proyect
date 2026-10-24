package fairteam_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import fairteam_backend.entity.Course;

public interface CourseRepository extends JpaRepository<Course, Long> {
    List<Course> findAllByOrderByCreatedAtDesc();
    boolean existsByNameIgnoreCase(String name);
}
