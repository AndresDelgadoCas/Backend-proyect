package fairteam_backend.service;

import fairteam_backend.entity.Grade;
import fairteam_backend.repository.GradeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;

    public GradeService(GradeRepository gradeRepository) {
        this.gradeRepository = gradeRepository;
    }

    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }

    public Grade save(Grade grade) {
        return gradeRepository.save(grade);
    }

    public Grade update(Long id, Grade grade) {
        return gradeRepository.findById(id)
                .map(existingGrade -> {
                    existingGrade.setGrade(grade.getGrade());
                    existingGrade.setPeriod(grade.getPeriod());
                    existingGrade.setStudent(grade.getStudent());
                    existingGrade.setSubject(grade.getSubject());
                    return gradeRepository.save(existingGrade);
                })
                .orElseThrow(() -> new RuntimeException("Grade not found with id: " + id));
    }

    public void delete(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new RuntimeException("Grade not found with id: " + id);
        }

        gradeRepository.deleteById(id);
    }
}