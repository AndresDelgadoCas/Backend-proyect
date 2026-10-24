package fairteam_backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import fairteam_backend.entity.Grade;
import fairteam_backend.service.GradeService;

@RestController
@RequestMapping("/api/grades")
@CrossOrigin(origins = "*")
public class GradeController {

    private final GradeService service;

    public GradeController(GradeService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Grade>> findAll(@RequestParam(required = false) Long courseId) {
        return ResponseEntity.ok(service.findAll(courseId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Grade> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Grade> create(@RequestBody Grade grade) {
        return ResponseEntity.ok(service.save(grade));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Grade> update(
            @PathVariable Long id,
            @RequestBody Grade grade) {

        return ResponseEntity.ok(service.update(id, grade));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/import")
    public ResponseEntity<List<Grade>> importGrades(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Long courseId) {

        if (file.isEmpty() || file.getSize() > 10L * 1024 * 1024) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Adjunta un CSV válido de máximo 10 MB.");
        }

        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(
                    "fairteam-grades-",
                    ".csv"
            );

            file.transferTo(tempFile.toFile());

            List<Grade> grades =
                    service.importFromCsv(tempFile.toString(), courseId);

            return ResponseEntity.ok(grades);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        } finally {
            if (tempFile != null) {
                try { Files.deleteIfExists(tempFile); }
                catch (IOException ignored) { /* Temporary files are cleaned up by the host. */ }
            }
        }
    }
}
