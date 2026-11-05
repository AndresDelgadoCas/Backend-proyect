package fairteam_backend.insights.domain.briefing;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import fairteam_backend.insights.domain.CourseAnalysis;
import fairteam_backend.insights.domain.CourseSummary;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/** Builder for the spoken daily briefing: each step adds one sentence only when it has something to say. */
public final class Briefing {
    private final List<String> sentences = new ArrayList<>();

    private Briefing() { }

    public static Briefing builder() { return new Briefing(); }

    public static String greeting(LocalTime now) {
        int hour = now.getHour();
        return hour < 12 ? "Buenos días" : hour < 19 ? "Buenas tardes" : "Buenas noches";
    }

    public Briefing greet(String teacher, LocalTime now) {
        String name = Grades.teacherFirstName(teacher);
        sentences.add(greeting(now) + (name.isEmpty() ? "." : ", " + name + "."));
        return this;
    }

    public Briefing state(CourseAnalysis a) {
        CourseSummary s = a.summary();
        if (s.students() == 0) { sentences.add(a.courseName() + " está listo, pero falta la lista de estudiantes. Importa el Excel del grupo y te digo cómo va cada uno."); return this; }
        if (s.gradeCount() == 0) { sentences.add("En " + a.courseName() + " tienes " + Grades.plural(s.students(), "estudiante", "estudiantes") + ", pero aún no hay notas. Cuando las cargues te digo quién necesita apoyo."); return this; }
        sentences.add("En " + a.courseName() + " el promedio va en " + Grades.format(s.average()) + ".");
        return this;
    }

    public Briefing support(CourseAnalysis a) {
        CourseSummary s = a.summary();
        if (s.gradeCount() == 0) return this;
        if (s.low() == 0) { sentences.add("Nadie está por debajo de tres."); return this; }
        List<String> first = a.students().stream().filter(p -> p.hasGrades() && p.level() == Level.LOW)
                .sorted(java.util.Comparator.comparingDouble(StudentPerformance::average)).limit(2)
                .map(p -> Grades.firstName(p.student().name())).toList();
        sentences.add(Grades.plural(s.low(), "estudiante necesita", "estudiantes necesitan") + " refuerzo"
                + (first.isEmpty() ? "" : "; empieza por " + String.join(" y ", first)) + ".");
        return this;
    }

    public Briefing subject(CourseAnalysis a) {
        SubjectAverage weakest = a.weakestSubject().orElse(null);
        if (weakest != null && weakest.average() < 3.5) sentences.add("El tema que más cuesta es " + weakest.name() + ", con " + Grades.format(weakest.average()) + ".");
        return this;
    }

    public Briefing trend(CourseAnalysis a) {
        int down = a.summary().fallingBehind();
        if (down > 0) sentences.add("Ojo: " + Grades.plural(down, "estudiante viene", "estudiantes vienen") + " bajando desde el primer corte.");
        return this;
    }

    public Briefing attendance(java.util.Map<Long, fairteam_backend.insights.domain.attendance.AttendanceStats> stats) {
        long alerts = stats.values().stream().filter(fairteam_backend.insights.domain.attendance.AttendanceStats::alert).count();
        if (alerts > 0) sentences.add("En asistencia, " + Grades.plural(alerts, "estudiante falta", "estudiantes faltan") + " a más del 20 % de las clases.");
        return this;
    }

    public Briefing nextStep(CourseAnalysis a, int materials) {
        CourseSummary s = a.summary();
        if (s.gradeCount() == 0) return this;
        if (s.low() > 0 && materials == 0) sentences.add("Sube el material del tema y te preparo el taller de refuerzo.");
        else if (s.low() > 0) sentences.add("Si quieres, te preparo ya un taller de refuerzo.");
        else sentences.add("Buen momento para proponer un reto a los que van mejor.");
        return this;
    }

    public String build() { return String.join(" ", sentences); }
}
