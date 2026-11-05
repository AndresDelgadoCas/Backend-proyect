package fairteam_backend.classroom;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.insights.application.port.out.AttendancePort;
import fairteam_backend.insights.domain.attendance.AttendanceStats;

/** Taking roll: one mark per student and session. Also feeds attendance to the assistant (AttendancePort). */
@Service
public class AttendanceService implements AttendancePort {
    private final AttendanceRepository repository;

    public AttendanceService(AttendanceRepository repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public Map<Long, AttendanceStatus> session(long courseId, LocalDate date) {
        requireCourse(courseId);
        return repository.forSession(courseId, date);
    }

    @Transactional
    public Map<Long, AttendanceStatus> save(long courseId, LocalDate date, Map<Long, AttendanceStatus> marks) {
        requireCourse(courseId);
        if (date.isAfter(LocalDate.now().plusDays(1))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No puedes registrar asistencia de una fecha futura.");
        }
        Set<Long> roster = new HashSet<>(repository.studentIds(courseId));
        if (!roster.containsAll(marks.keySet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Hay estudiantes que no pertenecen a este curso.");
        }
        if (marks.isEmpty()) repository.deleteSession(courseId, date);
        else repository.save(courseId, date, marks);
        return repository.forSession(courseId, date);
    }

    @Transactional(readOnly = true)
    public List<LocalDate> sessions(long courseId) {
        requireCourse(courseId);
        return repository.sessions(courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, AttendanceStats> statsFor(long courseId) { return repository.stats(courseId); }

    private void requireCourse(long courseId) {
        if (!repository.courseExists(courseId)) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró el curso.");
    }
}
