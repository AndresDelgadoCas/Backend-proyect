package fairteam_backend.rubrics;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Rubrics of a course: drafts with AI, assessment per student and publishing to the grade book. */
@RestController
@RequestMapping("/api/courses/{courseId}/rubrics")
public class RubricController {
    private final RubricService rubrics;

    public RubricController(RubricService rubrics) {
        this.rubrics = rubrics;
    }

    @GetMapping
    public List<Rubric.Summary> list(@PathVariable long courseId) {
        return rubrics.list(courseId);
    }

    @PostMapping("/generate")
    public Rubric.Draft generate(@PathVariable long courseId, @RequestBody Rubric.GenerateRequest body) {
        return rubrics.generate(courseId, body);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rubric.Detail create(@PathVariable long courseId, @RequestBody Rubric.Draft body) {
        return rubrics.create(courseId, body);
    }

    @GetMapping("/{rubricId}")
    public Rubric.Detail detail(@PathVariable long courseId, @PathVariable long rubricId) {
        return rubrics.detail(courseId, rubricId);
    }

    @PutMapping("/{rubricId}")
    public Rubric.Detail update(@PathVariable long courseId, @PathVariable long rubricId, @RequestBody Rubric.Draft body) {
        return rubrics.update(courseId, rubricId, body);
    }

    @DeleteMapping("/{rubricId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long courseId, @PathVariable long rubricId) {
        rubrics.delete(courseId, rubricId);
    }

    @PutMapping("/{rubricId}/students/{studentId}")
    public Rubric.StudentResult score(@PathVariable long courseId, @PathVariable long rubricId, @PathVariable long studentId,
            @RequestBody Rubric.ScoreRequest body) {
        return rubrics.score(courseId, rubricId, studentId, body);
    }

    @PostMapping("/{rubricId}/publish")
    public Rubric.PublishResult publish(@PathVariable long courseId, @PathVariable long rubricId, @RequestBody Rubric.PublishRequest body) {
        return rubrics.publish(courseId, rubricId, body);
    }
}
