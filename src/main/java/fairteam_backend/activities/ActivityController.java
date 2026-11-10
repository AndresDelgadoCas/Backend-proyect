package fairteam_backend.activities;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Saved workshops, quizzes and custom activities of a course, and everything related to a topic. */
@RestController
public class ActivityController {
    private final ActivityService activities;

    public ActivityController(ActivityService activities) {
        this.activities = activities;
    }

    @GetMapping("/api/courses/{courseId}/activities")
    public List<Activity.View> list(@PathVariable long courseId) {
        return activities.list(courseId);
    }

    @GetMapping("/api/courses/{courseId}/activities/latest")
    public Activity.View latest(@PathVariable long courseId, @RequestParam(required = false) String kind,
            @RequestParam(required = false) String topic) {
        return activities.latest(courseId, kind, topic);
    }

    @GetMapping("/api/courses/{courseId}/activities/{id}")
    public Activity.View get(@PathVariable long courseId, @PathVariable long id) {
        return activities.get(courseId, id);
    }

    @PostMapping("/api/courses/{courseId}/activities")
    @ResponseStatus(HttpStatus.CREATED)
    public Activity.View create(@PathVariable long courseId, @RequestBody Activity.Save body) {
        return activities.create(courseId, body);
    }

    @PutMapping("/api/courses/{courseId}/activities/{id}")
    public Activity.View update(@PathVariable long courseId, @PathVariable long id, @RequestBody Activity.Save body) {
        return activities.update(courseId, id, body);
    }

    @DeleteMapping("/api/courses/{courseId}/activities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long courseId, @PathVariable long id) {
        activities.delete(courseId, id);
    }

    @GetMapping("/api/courses/{courseId}/topics/{subjectId}")
    public Activity.TopicSummary topic(@PathVariable long courseId, @PathVariable long subjectId) {
        return activities.topic(courseId, subjectId);
    }
}
