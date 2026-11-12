package fairteam_backend.insights.domain.assistant;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Lets a data handler answer only when the question is about the course. Without it, a general question such
 * as "¿cuántos planetas tiene el sistema solar?" is hijacked by the nearest rule ("8 estudiantes necesitan
 * refuerzo") instead of reaching the AI.
 */
final class CourseTopicGate implements IntentHandler {
    private static final Pattern ABOUT_COURSE = Pattern.compile(
            "(^| )(estud|alumn|nota|calific|curso|grupo|clase|promedio|refuerz|seguimiento|asistenc|falta|tema|materia|"
                    + "quiz|taller|rubric|equipo|corte|riesgo|bajand|bajan|baja |mejora|mejoran|mejores|peores|peor|apoy|ayuda|"
                    + "necesit|aprob|reprob|pasar|perder|perdiendo|evalu|progreso|nivel|rendimiento|resumen|informe|"
                    + "quien (va|viene|esta|saco|tiene|lleva|sigue)|cuantos (son|hay|han|estan|tienen|llevan))");

    private final IntentHandler inner;

    CourseTopicGate(IntentHandler inner) { this.inner = inner; }

    static boolean aboutCourse(AssistantQuery q) {
        return q.student() != null || q.subject() != null || q.level() != null || ABOUT_COURSE.matcher(q.text()).find();
    }

    @Override
    public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
        return aboutCourse(q) ? inner.handle(q, c) : Optional.empty();
    }
}
