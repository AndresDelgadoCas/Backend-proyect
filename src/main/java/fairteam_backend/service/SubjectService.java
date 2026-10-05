package fairteam_backend.service;

import fairteam_backend.entity.Subject;
import fairteam_backend.repository.SubjectRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubjectService {

    private final SubjectRepository subjectRepository;

    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public List<Subject> findAll() {
        return subjectRepository.findAll();
    }

    public Optional<Subject> findById(Long id) {
        return subjectRepository.findById(id);
    }

    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }

    public Subject update(Long id, Subject subject) {
        return subjectRepository.findById(id)
                .map(existingSubject -> {
                    existingSubject.setName(subject.getName());
                    return subjectRepository.save(existingSubject);
                })
                .orElseThrow(() -> new RuntimeException("Subject not found with id: " + id));
    }

    public void delete(Long id) {
        if (!subjectRepository.existsById(id)) {
            throw new RuntimeException("Subject not found with id: " + id);
        }

        subjectRepository.deleteById(id);
    }
}