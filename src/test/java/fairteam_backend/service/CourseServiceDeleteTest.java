package fairteam_backend.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import fairteam_backend.entity.Course;
import fairteam_backend.repository.CourseRepository;
import fairteam_backend.repository.GradeRepository;
import fairteam_backend.repository.StudentRepository;

/** A course is independent: deleting it takes its students and grades along instead of refusing. */
class CourseServiceDeleteTest {
    private final CourseRepository courses = Mockito.mock(CourseRepository.class);
    private final StudentRepository students = Mockito.mock(StudentRepository.class);
    private final GradeRepository grades = Mockito.mock(GradeRepository.class);
    private final CourseService service = new CourseService(courses, students, grades);

    @Test
    void deletesAFullCourseWithItsStudentsAndGrades() {
        Course course = new Course();
        when(courses.findById(7L)).thenReturn(Optional.of(course));
        when(grades.subjectIdsOfCourse(7L)).thenReturn(List.of(1L, 2L));

        service.delete(7L);

        InOrder order = inOrder(grades, students, courses);
        order.verify(grades).deleteAllByCourseId(7L);
        order.verify(students).deleteAllByCourseId(7L);
        order.verify(courses).delete(course);
        verify(grades).deleteUnusedSubjects(List.of(1L, 2L));
    }

    @Test
    void skipsSubjectCleanupWhenTheCourseHadNoGrades() {
        Course course = new Course();
        when(courses.findById(8L)).thenReturn(Optional.of(course));
        when(grades.subjectIdsOfCourse(8L)).thenReturn(List.of());

        service.delete(8L);

        verify(courses).delete(course);
        verify(grades, never()).deleteUnusedSubjects(any());
    }
}
