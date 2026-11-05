package fairteam_backend.classroom;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses/{courseId}")
@CrossOrigin(origins = "*")
public class InterventionController {
    private final InterventionService service;

    public InterventionController(InterventionService service) { this.service = service; }

    public record NewIntervention(InterventionKind kind, String note) { }

    @GetMapping("/students/{studentId}/interventions")
    public List<InterventionService.Intervention> list(@PathVariable long courseId, @PathVariable long studentId) {
        return service.list(courseId, studentId);
    }

    @GetMapping("/interventions")
    public List<InterventionService.Intervention> recent(@PathVariable long courseId, @RequestParam(defaultValue = "20") int limit) {
        return service.recent(courseId, limit);
    }

    @PostMapping("/students/{studentId}/interventions")
    @ResponseStatus(HttpStatus.CREATED)
    public InterventionService.Intervention add(@PathVariable long courseId, @PathVariable long studentId, @RequestBody NewIntervention body) {
        return service.add(courseId, studentId, body.kind(), body.note());
    }

    @DeleteMapping("/students/{studentId}/interventions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long courseId, @PathVariable long studentId, @PathVariable long id) {
        service.delete(courseId, studentId, id);
    }
}
