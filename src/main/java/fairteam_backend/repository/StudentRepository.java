package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
}