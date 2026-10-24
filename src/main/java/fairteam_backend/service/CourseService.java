package fairteam_backend.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.entity.Course;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.repository.GradeRepository;
import fairteam_backend.repository.StudentRepository;

@Service
public class CourseService {
    private final CourseRepository courses;
    private final StudentRepository students;
    private final GradeRepository grades;

    public CourseService(CourseRepository courses, StudentRepository students, GradeRepository grades) {
        this.courses = courses;
        this.students = students;
        this.grades = grades;
    }

    @Transactional(readOnly = true)
    public List<Course> findAll() { return courses.findAllByOrderByCreatedAtDesc(); }

    @Transactional(readOnly = true)
    public Course findById(Long id) { return courses.findById(id).orElseThrow(() -> notFound()); }

    @Transactional
    public Course save(Course course) {
        validate(course);
        if (courses.existsByNameIgnoreCase(course.getName().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un curso con ese nombre.");
        }
        course.setName(course.getName().trim());
        return courses.save(course);
    }

    @Transactional
    public Course update(Long id, Course input) {
        validate(input);
        Course existing = findById(id);
        existing.setName(input.getName().trim());
        existing.setPeriod(input.getPeriod() == null ? null : input.getPeriod().trim());
        existing.setDescription(input.getDescription() == null ? null : input.getDescription().trim());
        return courses.save(existing);
    }

    @Transactional
    public void delete(Long id) {
        Course course = findById(id);
        if (students.existsByCourse_Id(id) || grades.existsByCourse_Id(id)) {
            throw new IllegalStateException("Mueve o elimina primero los estudiantes y las notas de este curso.");
        }
        courses.delete(course);
    }

    private static void validate(Course course) {
        if (course.getName() == null || course.getName().isBlank() || course.getName().trim().length() > 120) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre del curso es obligatorio (máximo 120 caracteres).");
        }
        if (course.getPeriod() != null && course.getPeriod().length() > 40) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El periodo no puede superar 40 caracteres.");
        }
        if (course.getDescription() != null && course.getDescription().length() > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La descripción no puede superar 500 caracteres.");
        }
    }

    private static ResponseStatusException notFound() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
    }
}
