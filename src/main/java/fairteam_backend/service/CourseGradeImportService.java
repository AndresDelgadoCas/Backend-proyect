package fairteam_backend.service;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.PushbackReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.entity.Course;
import fairteam_backend.entity.Grade;
import fairteam_backend.entity.Student;
import fairteam_backend.entity.Subject;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.repository.GradeRepository;
import fairteam_backend.repository.StudentRepository;
import fairteam_backend.repository.SubjectRepository;

@Service
public class CourseGradeImportService {
    private final CourseRepository courses;
    private final StudentRepository students;
    private final SubjectRepository subjects;
    private final GradeRepository grades;
    private final GradeObserverService observer;

    public CourseGradeImportService(CourseRepository courses, StudentRepository students, SubjectRepository subjects,
            GradeRepository grades, GradeObserverService observer) {
        this.courses = courses;
        this.students = students;
        this.subjects = subjects;
        this.grades = grades;
        this.observer = observer;
    }

    @Transactional
    public List<Grade> importGrades(Path csvPath, Long courseId) {
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso seleccionado."));
        try (BufferedReader reader = withoutBom(Files.newBufferedReader(csvPath, StandardCharsets.UTF_8));
                CSVParser parser = formatFor(reader).builder().setHeader().setSkipHeaderRecord(true).setTrim(true)
                        .setIgnoreHeaderCase(true).get().parse(reader)) {
            var headers = parser.getHeaderMap();
            if (!headers.containsKey("grade") || !headers.containsKey("period")
                    || !(headers.containsKey("studentEmail") || headers.containsKey("studentId") || headers.containsKey("studentName"))
                    || !(headers.containsKey("subject") || headers.containsKey("subjectId"))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "El CSV debe incluir grade, period, studentEmail (o studentId/studentName) y subject (o subjectId).");
            }
            List<Grade> imported = new ArrayList<>();
            int row = 1;
            for (CSVRecord record : parser) {
                row++;
                Student student = findStudent(record, course, row);
                Subject subject = findSubject(record, row);
                BigDecimal value = parseGrade(record, row);
                String period = field(record, "period");
                if (period.isBlank() || period.length() > 20) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El periodo no es válido en la fila " + row + ".");
                }
                Grade grade = grades.findByStudent_IdAndSubject_IdAndPeriod(student.getId(), subject.getId(), period)
                        .orElseGet(Grade::new);
                grade.setStudent(student);
                grade.setSubject(subject);
                grade.setCourse(course);
                grade.setGrade(value);
                grade.setPeriod(period);
                Grade saved = grades.save(grade);
                imported.add(saved);
                observer.notifyGradeChange(student.getId(), value.doubleValue());
            }
            if (imported.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no contiene notas.");
            return imported;
        } catch (IOException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el archivo CSV. Guarda el archivo como UTF-8.");
        }
    }

    private Student findStudent(CSVRecord row, Course course, int number) {
        String email = field(row, "studentEmail");
        Student student;
        if (!email.isBlank()) {
            student = students.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "No existe el correo del estudiante en la fila " + number + ". Primero importa la lista de estudiantes."));
        } else if (!field(row, "studentName").isBlank()) {
            String name = field(row, "studentName");
            List<Student> matches = students.findAllByCourse_IdOrderByNameAsc(course.getId()).stream()
                    .filter(candidate -> candidate.getName().equalsIgnoreCase(name)).toList();
            if (matches.size() > 1) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Hay varios estudiantes llamados " + name + " en la fila " + number + "; usa studentEmail para distinguirlos.");
            }
            if (matches.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No existe el nombre del estudiante en la fila " + number + ". Primero importa la lista de estudiantes.");
            }
            student = matches.get(0);
        } else {
            Long id = parseId(field(row, "studentId"), "studentId", number);
            student = students.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No existe el estudiante de la fila " + number + "."));
        }
        if (student.getCourseId() != null && !student.getCourseId().equals(course.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El estudiante de la fila " + number + " pertenece a otro curso.");
        }
        if (student.getCourseId() == null) { student.setCourse(course); student = students.save(student); }
        return student;
    }

    private Subject findSubject(CSVRecord row, int number) {
        String name = field(row, "subject");
        if (!name.isBlank()) {
            return subjects.findByNameIgnoreCase(name).orElseGet(() -> {
                Subject subject = new Subject();
                subject.setName(name.substring(0, Math.min(name.length(), 120)));
                return subjects.save(subject);
            });
        }
        Long id = parseId(field(row, "subjectId"), "subjectId", number);
        return subjects.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "No existe la materia de la fila " + number + "."));
    }

    private static BigDecimal parseGrade(CSVRecord row, int number) {
        try {
            BigDecimal grade = new BigDecimal(field(row, "grade").replace(',', '.'));
            if (grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(BigDecimal.valueOf(5)) > 0) throw new NumberFormatException();
            return grade.setScale(2, java.math.RoundingMode.HALF_UP);
        } catch (NumberFormatException | ArithmeticException error) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La nota de la fila " + number + " debe estar entre 0 y 5.");
        }
    }

    private static Long parseId(String text, String field, int row) {
        try { return Long.valueOf(text); }
        catch (NumberFormatException error) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " no válido en la fila " + row + "."); }
    }

    private static String field(CSVRecord record, String name) {
        return record.isMapped(name) ? record.get(name).trim() : "";
    }

    private static BufferedReader withoutBom(java.io.Reader input) throws IOException {
        PushbackReader pushback = new PushbackReader(input, 1);
        int first = pushback.read();
        if (first != -1 && first != '\uFEFF') pushback.unread(first);
        return new BufferedReader(pushback);
    }

    private static CSVFormat formatFor(BufferedReader reader) throws IOException {
        reader.mark(8192);
        String header = reader.readLine();
        reader.reset();
        if (header == null) return CSVFormat.DEFAULT;
        int commas = 0, semicolons = 0;
        boolean quoted = false;
        for (int i = 0; i < header.length(); i++) {
            char current = header.charAt(i);
            if (current == '"') quoted = !quoted;
            else if (!quoted && current == ',') commas++;
            else if (!quoted && current == ';') semicolons++;
        }
        return CSVFormat.DEFAULT.builder().setDelimiter(semicolons > commas ? ';' : ',').get();
    }
}
