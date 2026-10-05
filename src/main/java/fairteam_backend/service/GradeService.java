package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.Grade;
import fairteam_backend.repository.GradeRepository;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeObserverService gradeObserverService;

    public GradeService(
            GradeRepository gradeRepository,
            GradeObserverService gradeObserverService) {

        this.gradeRepository = gradeRepository;
        this.gradeObserverService = gradeObserverService;
    }

    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }

    public Grade save(Grade grade) {

        Grade savedGrade = gradeRepository.save(grade);

        if (savedGrade.getStudentId() != null
                && savedGrade.getGrade() != null) {

            gradeObserverService.notifyGradeChange(
                    savedGrade.getStudentId(),
                    savedGrade.getGrade().doubleValue()
            );
        }

        return savedGrade;
    }

    public Grade update(Long id, Grade grade) {

        return gradeRepository.findById(id)
                .map(existingGrade -> {

                    existingGrade.setGrade(grade.getGrade());
                    existingGrade.setPeriod(grade.getPeriod());
                    existingGrade.setStudent(grade.getStudent());
                    existingGrade.setSubject(grade.getSubject());

                    Grade updatedGrade =
                            gradeRepository.save(existingGrade);

                    if (updatedGrade.getStudentId() != null
                            && updatedGrade.getGrade() != null) {

                        gradeObserverService.notifyGradeChange(
                                updatedGrade.getStudentId(),
                                updatedGrade.getGrade().doubleValue()
                        );
                    }

                    return updatedGrade;
                })
                .orElseThrow(() ->
                        new RuntimeException(
                                "Grade not found with id: " + id
                        ));
    }

    public void delete(Long id) {

        if (!gradeRepository.existsById(id)) {
            throw new RuntimeException(
                    "Grade not found with id: " + id
            );
        }

        gradeRepository.deleteById(id);
    }
}