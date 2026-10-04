package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.StudentAvailability;

public interface StudentAvailabilityRepository extends JpaRepository<StudentAvailability, Long> {
}