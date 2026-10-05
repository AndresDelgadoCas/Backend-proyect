package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.WorkshopRecommendation;
import fairteam_backend.factory.Recommendation;
import fairteam_backend.repository.WorkshopRecommendationRepository;

@Service
public class WorkshopRecommendationService {

    private final WorkshopRecommendationRepository repository;
    private final RecommendationService recommendationService;

    public WorkshopRecommendationService(
            WorkshopRecommendationRepository repository,
            RecommendationService recommendationService) {

        this.repository = repository;
        this.recommendationService = recommendationService;
    }

    public List<WorkshopRecommendation> findAll() {
        return repository.findAll();
    }

    public Optional<WorkshopRecommendation> findById(Long id) {
        return repository.findById(id);
    }

    public WorkshopRecommendation save(
            WorkshopRecommendation recommendation) {

        return repository.save(recommendation);
    }

    public WorkshopRecommendation update(
            Long id,
            WorkshopRecommendation recommendation) {

        return repository.findById(id)
                .map(existing -> {

                    existing.setStudent(
                            recommendation.getStudent()
                    );

                    existing.setWorkshop(
                            recommendation.getWorkshop()
                    );

                    existing.setReason(
                            recommendation.getReason()
                    );

                    existing.setRecommendationScore(
                            recommendation.getRecommendationScore()
                    );

                    return repository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException(
                                "Workshop recommendation not found with id: "
                                        + id
                        ));
    }

    public void delete(Long id) {

        if (!repository.existsById(id)) {
            throw new RuntimeException(
                    "Workshop recommendation not found with id: "
                            + id
            );
        }

        repository.deleteById(id);
    }

    public Recommendation createWorkshopRecommendation(
            String message) {

        return recommendationService
                .createWorkshopRecommendation(message);
    }
}