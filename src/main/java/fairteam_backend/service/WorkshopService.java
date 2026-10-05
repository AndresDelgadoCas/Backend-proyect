package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.Workshop;
import fairteam_backend.repository.WorkshopRepository;

@Service
public class WorkshopService {

    private final WorkshopRepository workshopRepository;

    public WorkshopService(WorkshopRepository workshopRepository) {
        this.workshopRepository = workshopRepository;
    }

    public List<Workshop> findAll() {
        return workshopRepository.findAll();
    }

    public Optional<Workshop> findById(Long id) {
        return workshopRepository.findById(id);
    }

    public Workshop save(Workshop workshop) {
        return workshopRepository.save(workshop);
    }

    public Workshop update(Long id, Workshop workshop) {
        return workshopRepository.findById(id)
                .map(existing -> {
                    existing.setName(workshop.getName());
                    existing.setDescription(workshop.getDescription());
                    return workshopRepository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException("Workshop not found with id: " + id));
    }

    public void delete(Long id) {
        if (!workshopRepository.existsById(id)) {
            throw new RuntimeException("Workshop not found with id: " + id);
        }

        workshopRepository.deleteById(id);
    }
}