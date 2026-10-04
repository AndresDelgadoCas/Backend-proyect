package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.Subject;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
}