package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.Workshop;

public interface WorkshopRepository extends JpaRepository<Workshop, Long> {
}