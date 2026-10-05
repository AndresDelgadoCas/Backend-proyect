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

import fairteam_backend.entity.StudentAvailability;
import fairteam_backend.service.StudentAvailabilityService;

@RestController
@RequestMapping("/api/student-availability")
@CrossOrigin(origins = "*")
public class StudentAvailabilityController {

    private final StudentAvailabilityService service;

    public StudentAvailabilityController(StudentAvailabilityService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StudentAvailability>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<StudentAvailability> findById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentAvailability> create(
            @RequestBody StudentAvailability availability) {
        return ResponseEntity.ok(service.save(availability));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StudentAvailability> update(
            @PathVariable Long id,
            @RequestBody StudentAvailability availability) {
        return ResponseEntity.ok(service.update(id, availability));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}