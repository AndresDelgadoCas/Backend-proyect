package fairteam_backend.insights.domain;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fairteam_backend.insights.domain.risk.RiskScoringStrategy;

/**
 * Domain service that turns raw grades into a course reading. Pure Java: no Spring, no JPA,
 * so it is tested in isolation. The risk formula is injected as a Strategy.
 */
public class CourseAnalyzer {
    private final RiskScoringStrategy riskStrategy;

    public CourseAnalyzer(RiskScoringStrategy riskStrategy) { this.riskStrategy = riskStrategy; }

    public CourseAnalysis analyze(long courseId, String courseName, List<StudentRef> students, List<GradeRecord> grades) {
        List<String> periods = PeriodOrder.of(grades);
        Map<Long, List<GradeRecord>> byStudent = grades.stream().collect(Collectors.groupingBy(GradeRecord::studentId));

        List<StudentPerformance> performances = new ArrayList<>();
        for (StudentRef student : students) {
            performances.add(perform(student, byStudent.getOrDefault(student.id(), List.of()), periods));
        }
        performances.sort(Comparator.comparingInt(StudentPerformance::risk).reversed()
                .thenComparing(p -> p.student().name(), String.CASE_INSENSITIVE_ORDER));

        List<SubjectAverage> subjects = subjectAverages(grades);
        List<StudentPerformance> graded = performances.stream().filter(StudentPerformance::hasGrades).toList();
        CourseSummary summary = new CourseSummary(
                performances.size(),
                graded.size(),
                performances.size() - graded.size(),
                (int) graded.stream().filter(p -> p.level() == Level.LOW).count(),
                (int) graded.stream().filter(p -> p.level() == Level.MEDIUM).count(),
                (int) graded.stream().filter(p -> p.level() == Level.HIGH).count(),
                grades.isEmpty() ? null : Grades.average(grades.stream().map(GradeRecord::value).toList()),
                grades.size(),
                (int) performances.stream().filter(p -> p.direction() == Direction.DOWN).count());
        return new CourseAnalysis(courseId, courseName, summary, periods, subjects, List.copyOf(performances), GradeBand.of(performances));
    }

    private StudentPerformance perform(StudentRef student, List<GradeRecord> own, List<String> periods) {
        if (own.isEmpty()) {
            Level profile = Level.fromProfile(student.profileLevel());
            int risk = riskStrategy.score(null, Direction.UNKNOWN, 0);
            return new StudentPerformance(student, null, 0, profile, List.of(), null, Direction.UNKNOWN, risk, List.of());
        }
        double average = Grades.average(own.stream().map(GradeRecord::value).toList());
        List<PeriodAverage> points = new ArrayList<>();
        for (String period : periods) {
            List<Double> marks = own.stream().filter(g -> PeriodOrder.normalize(g.period()).equals(period)).map(GradeRecord::value).toList();
            if (!marks.isEmpty()) points.add(new PeriodAverage(period, Grades.average(marks)));
        }
        Double delta = points.size() >= 2 ? points.get(points.size() - 1).average() - points.get(0).average() : null;
        Direction direction = Direction.of(delta);
        return new StudentPerformance(student, average, own.size(), Level.fromAverage(average), List.copyOf(points), delta,
                direction, riskStrategy.score(average, direction, delta == null ? 0 : delta), subjectAverages(own));
    }

    private static List<SubjectAverage> subjectAverages(List<GradeRecord> grades) {
        Map<Long, List<GradeRecord>> bySubject = new LinkedHashMap<>();
        for (GradeRecord grade : grades) bySubject.computeIfAbsent(grade.subjectId(), id -> new ArrayList<>()).add(grade);
        return bySubject.entrySet().stream().map(entry -> {
            List<GradeRecord> list = entry.getValue();
            Map<Long, Double> perStudent = list.stream().collect(Collectors.groupingBy(GradeRecord::studentId,
                    Collectors.averagingDouble(GradeRecord::value)));
            int below = (int) perStudent.values().stream().filter(avg -> Grades.round1(avg) < Grades.PASSING).count();
            return new SubjectAverage(entry.getKey(), list.get(0).subjectName(),
                    Grades.average(list.stream().map(GradeRecord::value).toList()), list.size(), below);
        }).sorted(Comparator.comparingDouble(SubjectAverage::average)).toList();
    }
}
