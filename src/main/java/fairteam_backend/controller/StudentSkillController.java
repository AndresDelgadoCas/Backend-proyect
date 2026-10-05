package fairteam_backend.controller;

import fairteam_backend.entity.StudentSkill;
import fairteam_backend.entity.StudentSkillId;
import fairteam_backend.service.StudentSkillService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-skills")
@CrossOrigin(origins = "*")
public class StudentSkillController {

    private final StudentSkillService service;

    public StudentSkillController(StudentSkillService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<StudentSkill>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{studentId}/{skillId}")
    public ResponseEntity<StudentSkill> findById(
            @PathVariable Long studentId,
            @PathVariable Long skillId) {

        StudentSkillId id = new StudentSkillId(studentId, skillId);

        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<StudentSkill> create(
            @RequestBody StudentSkill studentSkill) {
        return ResponseEntity.ok(service.save(studentSkill));
    }

    @DeleteMapping("/{studentId}/{skillId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long studentId,
            @PathVariable Long skillId) {

        StudentSkillId id = new StudentSkillId(studentId, skillId);
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}