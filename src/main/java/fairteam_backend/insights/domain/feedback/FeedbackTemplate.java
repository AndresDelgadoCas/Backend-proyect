package fairteam_backend.insights.domain.feedback;

import java.util.ArrayList;
import java.util.List;

import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/**
 * Template Method: the structure of a feedback message is fixed (greeting, body, plan, closing);
 * each tone only decides how it greets and signs off. {@link #forTone} is the factory method.
 */
public abstract class FeedbackTemplate {

    public static FeedbackTemplate forTone(FeedbackTone tone) {
        return tone == FeedbackTone.FORMAL ? new FormalFeedback() : new WarmFeedback();
    }

    public final String compose(StudentPerformance student, GradeProjection projection, String course, String teacher) {
        List<String> lines = new ArrayList<>();
        lines.add(greeting(student));
        lines.add("");
        lines.addAll(body(student, projection, course));
        lines.add("");
        lines.add(closing());
        lines.add(teacher);
        return String.join("\n", lines);
    }

    protected abstract String greeting(StudentPerformance student);

    protected abstract String closing();

    private List<String> body(StudentPerformance s, GradeProjection p, String course) {
        List<String> lines = new ArrayList<>();
        if (!s.hasGrades()) {
            lines.add("Aún no tengo notas tuyas registradas en " + course + ". Si entregaste alguna actividad que no aparece, cuéntame para revisarlo.");
            return lines;
        }
        if (s.level() == Level.HIGH) {
            lines.add("Quería felicitarte: vas con un promedio de " + Grades.format(s.average()) + " en " + course + "."
                    + (s.direction() == Direction.UP ? " Además se nota que has mejorado de un corte a otro." : ""));
            lines.add("Si quieres un reto adicional, puedo proponerte un ejercicio de profundización.");
            return lines;
        }
        String trend = s.direction() == Direction.DOWN ? " y ha bajado respecto al primer corte" : s.direction() == Direction.UP ? " y vienes mejorando, ¡bien!" : "";
        lines.add("Te escribo para acompañarte en " + course + ". Tu promedio va en " + Grades.format(s.average()) + trend + ".");
        SubjectAverage weakest = s.weakest();
        if (weakest != null) lines.add("El tema donde más te está costando es " + weakest.name() + " (" + Grades.format(weakest.average()) + ").");
        if (p != null && p.needed() != null && (p.status() == ProjectionStatus.POSSIBLE || p.status() == ProjectionStatus.HARD)) {
            lines.add("Para llegar a 3,0 necesitas " + Grades.format(p.needed()) + " en lo que falta del semestre. "
                    + (p.status() == ProjectionStatus.HARD ? "Es exigente, pero se puede si empezamos ya." : "Es totalmente alcanzable."));
        }
        lines.add("");
        lines.add("Te propongo:");
        lines.add("• Repasar " + (weakest != null ? weakest.name() : "los temas recientes") + " con el taller que te voy a compartir.");
        lines.add("• Venir a asesoría esta semana para resolver dudas.");
        lines.add("• Contarme si hay algo que te esté dificultando el curso.");
        return lines;
    }
}
