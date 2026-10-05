package fairteam_backend.controller;

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
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.entity.StudentProjectPreference;
import fairteam_backend.service.StudentProjectPreferenceService;

@RestController
@RequestMapping("/api/student-project-preferences")
@CrossOrigin(origins = "*")
public class StudentProjectPreferenceController {

    private final StudentProjectPreferenceService service;

    public StudentProjectPreferenceController(
            StudentProjectPreferenceService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StudentProjectPreference>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentProjectPreference> findById(
            @PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentProjectPreference> create(
            @RequestBody StudentProjectPreference preference) {
        return ResponseEntity.ok(service.save(preference));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentProjectPreference> update(
            @PathVariable Long id,
            @RequestBody StudentProjectPreference preference) {
        return ResponseEntity.ok(service.update(id, preference));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}