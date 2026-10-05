package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.StudentAvailability;
import fairteam_backend.repository.StudentAvailabilityRepository;

@Service
public class StudentAvailabilityService {

    private final StudentAvailabilityRepository repository;

    public StudentAvailabilityService(StudentAvailabilityRepository repository) {
        this.repository = repository;
    }

    public List<StudentAvailability> findAll() {
        return repository.findAll();
    }

    public Optional<StudentAvailability> findById(Long id) {
        return repository.findById(id);
    }

    public StudentAvailability save(StudentAvailability availability) {
        return repository.save(availability);
    }

    public StudentAvailability update(Long id, StudentAvailability availability) {
        return repository.findById(id)
                .map(existing -> {
                    existing.setStudent(availability.getStudent());
                    existing.setDayOfWeek(availability.getDayOfWeek());
                    existing.setStartTime(availability.getStartTime());
                    existing.setEndTime(availability.getEndTime());
                    return repository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException("Availability not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Availability not found with id: " + id);
        }

        repository.deleteById(id);
    }
}