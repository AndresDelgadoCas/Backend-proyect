package fairteam_backend.service;

import fairteam_backend.entity.Student;
import fairteam_backend.repository.StudentRepository;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.entity.Course;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;

    public StudentService(StudentRepository studentRepository, CourseRepository courseRepository) {
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
    }

    public List<Student> findAll() {
        return studentRepository.findAll();
    }

    public List<Student> findAll(Long courseId) {
        return courseId == null ? findAll() : studentRepository.findAllByCourse_IdOrderByNameAsc(courseId);
    }

    public Optional<Student> findById(Long id) {
        return studentRepository.findById(id);
    }

    public Student save(Student student) {
        assignCourse(student);
        return studentRepository.save(student);
    }

    public Student update(Long id, Student student) {
        return studentRepository.findById(id)
                .map(existingStudent -> {
                    existingStudent.setName(student.getName());
                    existingStudent.setEmail(student.getEmail());
                    if (student.getCourseId() != null) {
                        assignCourse(student);
                        existingStudent.setCourse(student.getCourse());
                    }
                    return studentRepository.save(existingStudent);
                })
                .orElseThrow(() -> new RuntimeException("Student not found with id: " + id));
    }

    private void assignCourse(Student student) {
        Long courseId = student.getCourseId();
        if (courseId == null) return;
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso seleccionado."));
        student.setCourse(course);
    }

    public void delete(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new RuntimeException("Student not found with id: " + id);
        }

        studentRepository.deleteById(id);
    }
}
