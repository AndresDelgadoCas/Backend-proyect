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

import fairteam_backend.entity.TeamStudent;
import fairteam_backend.entity.TeamStudentId;
import fairteam_backend.service.TeamStudentService;

@RestController
@RequestMapping("/api/team-students")
@CrossOrigin(origins = "*")
public class TeamStudentController {

    private final TeamStudentService service;

    public TeamStudentController(TeamStudentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<TeamStudent>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{teamId}/{studentId}")
    public ResponseEntity<TeamStudent> findById(
            @PathVariable Long teamId,
            @PathVariable Long studentId) {

        TeamStudentId id = new TeamStudentId(teamId, studentId);

        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TeamStudent> create(
            @RequestBody TeamStudent teamStudent) {
        return ResponseEntity.ok(service.save(teamStudent));
    }

    @DeleteMapping("/{teamId}/{studentId}")
    public ResponseEntity<Void> delete(
            @PathVariable Long teamId,
            @PathVariable Long studentId) {

        TeamStudentId id = new TeamStudentId(teamId, studentId);
        service.delete(id);

        return ResponseEntity.noContent().build();
    }
}