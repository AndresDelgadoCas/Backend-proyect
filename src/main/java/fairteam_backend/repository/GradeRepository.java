package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.Grade;

public interface GradeRepository extends JpaRepository<Grade, Long> {
}