package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.StudentProjectPreference;
import fairteam_backend.repository.StudentProjectPreferenceRepository;

@Service
public class StudentProjectPreferenceService {

    private final StudentProjectPreferenceRepository repository;

    public StudentProjectPreferenceService(StudentProjectPreferenceRepository repository) {
        this.repository = repository;
    }

    public List<StudentProjectPreference> findAll() {
        return repository.findAll();
    }

    public Optional<StudentProjectPreference> findById(Long id) {
        return repository.findById(id);
    }

    public StudentProjectPreference save(StudentProjectPreference preference) {
        return repository.save(preference);
    }

    public StudentProjectPreference update(
            Long id,
            StudentProjectPreference preference) {

        return repository.findById(id)
                .map(existing -> {
                    existing.setStudent(preference.getStudent());
                    existing.setProject(preference.getProject());
                    existing.setPreferenceType(preference.getPreferenceType());
                    return repository.save(existing);
                })
                .orElseThrow(() ->
                        new RuntimeException(
                                "Student project preference not found with id: " + id));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException(
                    "Student project preference not found with id: " + id);
        }

        repository.deleteById(id);
    }
}