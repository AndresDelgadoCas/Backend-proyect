package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.adapter.CsvGradeAdapter;
import fairteam_backend.adapter.GradeAdapter;
import fairteam_backend.entity.Grade;
import fairteam_backend.repository.GradeRepository;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.entity.Course;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GradeService {

    private final GradeRepository gradeRepository;
    private final GradeObserverService gradeObserverService;
    private final CourseRepository courseRepository;
    private final CourseGradeImportService courseGradeImportService;

    public GradeService(
            GradeRepository gradeRepository,
            GradeObserverService gradeObserverService,
            CourseRepository courseRepository,
            CourseGradeImportService courseGradeImportService) {

        this.gradeRepository = gradeRepository;
        this.gradeObserverService = gradeObserverService;
        this.courseRepository = courseRepository;
        this.courseGradeImportService = courseGradeImportService;
    }

    public List<Grade> findAll() {
        return gradeRepository.findAll();
    }

    public List<Grade> findAll(Long courseId) {
        return courseId == null ? findAll() : gradeRepository.findAllByCourse_Id(courseId);
    }

    public Optional<Grade> findById(Long id) {
        return gradeRepository.findById(id);
    }

    public Grade save(Grade grade) {

        assignCourse(grade);

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
                    if (grade.getCourseId() != null) {
                        assignCourse(grade);
                        existingGrade.setCourse(grade.getCourse());
                    }

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

    private void assignCourse(Grade grade) {
        Long courseId = grade.getCourseId();
        if (courseId == null) return;
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso seleccionado."));
        grade.setCourse(course);
    }

    public List<Grade> importFromCsv(String filePath) {
        return importFromCsv(filePath, null);
    }

    public List<Grade> importFromCsv(String filePath, Long courseId) {

        if (courseId != null) return courseGradeImportService.importGrades(java.nio.file.Path.of(filePath), courseId);

        GradeAdapter adapter = new CsvGradeAdapter();

        List<Grade> grades = adapter.importGrades(filePath);

        if (courseId != null) {
            for (Grade grade : grades) {
                grade.setCourseId(courseId);
                assignCourse(grade);
            }
        }

        List<Grade> savedGrades =
                gradeRepository.saveAll(grades);

        for (Grade grade : savedGrades) {

            if (grade.getStudentId() != null
                    && grade.getGrade() != null) {

                gradeObserverService.notifyGradeChange(
                        grade.getStudentId(),
                        grade.getGrade().doubleValue()
                );
            }
        }

        return savedGrades;
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
