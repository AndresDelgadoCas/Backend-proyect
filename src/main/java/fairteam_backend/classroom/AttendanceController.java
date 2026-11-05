package fairteam_backend.classroom;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.insights.domain.attendance.AttendanceStats;

@RestController
@RequestMapping("/api/courses/{courseId}/attendance")
@CrossOrigin(origins = "*")
public class AttendanceController {
    private final AttendanceService service;

    public AttendanceController(AttendanceService service) { this.service = service; }

    public record Mark(long studentId, AttendanceStatus status) { }

    public record Summary(long studentId, int sessions, int present, int absent, int late, int excused, int percent, boolean alert) {
        static Summary of(AttendanceStats s) {
            return new Summary(s.studentId(), s.sessions(), s.present(), s.absent(), s.late(), s.excused(), s.percent(), s.alert());
        }
    }

    @GetMapping
    public List<Mark> session(@PathVariable long courseId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return marks(service.session(courseId, date));
    }

    @PutMapping
    public List<Mark> save(@PathVariable long courseId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestBody Collection<Mark> marks) {
        Map<Long, AttendanceStatus> byStudent = marks.stream()
                .collect(Collectors.toMap(Mark::studentId, Mark::status, (a, b) -> b));
        return marks(service.save(courseId, date, byStudent));
    }

    @GetMapping("/sessions")
    public List<LocalDate> sessions(@PathVariable long courseId) { return service.sessions(courseId); }

    @GetMapping("/summary")
    public List<Summary> summary(@PathVariable long courseId) {
        service.sessions(courseId);
        return service.statsFor(courseId).values().stream().map(Summary::of).toList();
    }

    private static List<Mark> marks(Map<Long, AttendanceStatus> marks) {
        return marks.entrySet().stream().map(e -> new Mark(e.getKey(), e.getValue())).toList();
    }
}
