package fairteam_backend.insights.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.insights.application.port.in.TeacherOverviewUseCase;

/** GET /api/insights/overview: all of the signed-in teacher's courses (row level security filters them). */
@RestController
public class TeacherOverviewController {
    private final TeacherOverviewUseCase overview;

    public TeacherOverviewController(TeacherOverviewUseCase overview) { this.overview = overview; }

    @GetMapping("/api/insights/overview")
    public List<TeacherOverviewUseCase.CourseOverview> overview() { return overview.overview(); }
}
