package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.WorkshopRecommendation;

public interface WorkshopRecommendationRepository
        extends JpaRepository<WorkshopRecommendation, Long> {
}