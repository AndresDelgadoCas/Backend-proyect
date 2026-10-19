
package fairteam_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.entity.evaluation.AssignmentSubmission;
import fairteam_backend.service.AssignmentSubmissionService;

@RestController
@RequestMapping("/api/assignment-submissions")
@CrossOrigin(origins = "*")
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService service;

    public AssignmentSubmissionController(AssignmentSubmissionService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<AssignmentSubmission>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AssignmentSubmission> findById(
            @PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<AssignmentSubmission>> findByAssignmentId(
            @PathVariable Long assignmentId) {
        return ResponseEntity.ok(service.findByAssignmentId(assignmentId));
    }

    @GetMapping("/student/{studentId}")
    public ResponseEntity<List<AssignmentSubmission>> findByStudentId(
            @PathVariable Long studentId) {
        return ResponseEntity.ok(service.findByStudentId(studentId));
    }

    @PostMapping
    public ResponseEntity<AssignmentSubmission> create(
            @RequestBody AssignmentSubmission submission) {
        return ResponseEntity.ok(service.save(submission));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
