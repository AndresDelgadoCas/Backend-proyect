package fairteam_backend.service;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PushbackReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.entity.Course;
import fairteam_backend.entity.Student;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.repository.StudentRepository;

@Service
public class StudentRosterImportService {
    private final StudentRepository students;
    private final CourseRepository courses;

    public StudentRosterImportService(StudentRepository students, CourseRepository courses) {
        this.students = students;
        this.courses = courses;
    }

    @Transactional
    public List<Student> importRoster(Long courseId, MultipartFile file) {
        if (courseId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona un curso para importar la lista.");
        Course course = courses.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso seleccionado."));
        if (file == null || file.isEmpty() || file.getSize() > 5L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjunta un CSV válido de máximo 5 MB.");
        }
        try (BufferedReader reader = withoutBom(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
                CSVParser parser = formatFor(reader).builder().setHeader().setSkipHeaderRecord(true).setTrim(true)
                        .setIgnoreHeaderCase(true).get().parse(reader)) {
            if (!parser.getHeaderMap().containsKey("name")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El CSV de estudiantes debe incluir el encabezado name; email y academicLevel son opcionales.");
            }
            List<Student> imported = new ArrayList<>();
            int row = 1;
            for (CSVRecord record : parser) {
                row++;
                String name = value(record, "name");
                if (name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el nombre del estudiante en la fila " + row + ".");
                String email = value(record, "email");
                Student student = email.isBlank() ? null : students.findByEmailIgnoreCase(email).orElse(null);
                if (student != null && student.getCourseId() != null && !student.getCourseId().equals(courseId)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "El correo " + email + " ya pertenece a otro curso.");
                }
                if (student == null) student = new Student();
                student.setName(name.substring(0, Math.min(name.length(), 120)));
                student.setEmail(email.isBlank() ? null : email.toLowerCase(Locale.ROOT));
                student.setCourse(course);
                String level = value(record, "academicLevel");
                if (!level.isBlank()) student.setAcademicLevel(normalizeLevel(level, row));
                imported.add(students.save(student));
            }
            if (imported.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no contiene estudiantes.");
            return imported;
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo leer el CSV. Guarda el archivo como UTF-8 e inténtalo de nuevo.");
        }
    }

    private static String value(CSVRecord record, String name) {
        return record.isMapped(name) ? record.get(name).trim() : "";
    }

    private static BufferedReader withoutBom(InputStreamReader input) throws IOException {
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

    private static String normalizeLevel(String value, int row) {
        return switch (value.trim().toUpperCase(Locale.ROOT)) {
            case "LOW", "BAJO", "BAJA" -> "LOW";
            case "MEDIUM", "INTERMEDIO", "MEDIA" -> "MEDIUM";
            case "HIGH", "ALTO", "ALTA" -> "HIGH";
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nivel académico no válido en la fila " + row + ". Usa BAJO, MEDIO o ALTO.");
        };
    }
}
