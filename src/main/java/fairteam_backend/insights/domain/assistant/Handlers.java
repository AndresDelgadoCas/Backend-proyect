package fairteam_backend.insights.domain.assistant;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import fairteam_backend.insights.domain.CourseSummary;
import fairteam_backend.insights.domain.Direction;
import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.PeriodAverage;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;
import fairteam_backend.insights.domain.grading.GradeProjection;
import fairteam_backend.insights.domain.grading.ProjectionStatus;

/** The assistant's skills, one handler per intent, in the order {@link AssistantEngine} asks them. */
final class Handlers {
    private Handlers() { }

    static ReplyItem item(StudentPerformance p, String value, String note) {
        String tone = !p.hasGrades() ? "none" : switch (p.level()) { case LOW -> "low"; case MEDIUM -> "medium"; case HIGH -> "high"; default -> "none"; };
        return new ReplyItem(p.student().id(), p.student().name(), value, note, tone);
    }

    static String path(StudentPerformance p) {
        return String.join(" → ", p.periods().stream().map(PeriodAverage::average).map(Grades::format).toList());
    }

    static String levelPhrase(Level level) {
        return switch (level) { case LOW -> "de refuerzo"; case MEDIUM -> "para los que están en seguimiento"; case HIGH -> "de reto"; default -> ""; };
    }

    static ReplyAction workshop(String label, String mode, Level level, String topic, StudentPerformance student) {
        return ReplyAction.of("workshop", label, "mode", mode, "level", level.name(), "topic", topic, "studentId", student == null ? null : String.valueOf(student.student().id()));
    }

    static final class NeedsGrades implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (c.analysis().summary().gradeCount() > 0 || q.has("equipo", "importa", "sube", "material", "abre", "ir a", "llevame")) return Optional.empty();
            return Optional.of(AssistantReply.say("needs-grades", "Aún no hay notas en " + c.analysis().courseName() + ". Cárgalas y podré responderte eso.")
                    .withActions(ReplyAction.of("navigate", "Ir a estudiantes y notas", "route", "estudiantes")));
        }
    }

    static final class PassingGrade implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("que nota", "cuanto debe", "cuanto necesita", "cuanto le falta", "necesita para", "necesita sacar", "para pasar", "para aprobar", "alcanza", "pierde", "perder", "aprueba", "gana el")) return Optional.empty();
            ReplyAction simulator = ReplyAction.of("navigate", "Abrir simulador", "route", "seguimiento", "vista", "simulador");
            if (q.student() != null) {
                StudentPerformance s = q.student();
                GradeProjection p = c.projections().get(s.student().id());
                return Optional.of(AssistantReply.say("passing-grade", s.student().name() + ": " + (p != null ? p.explanation() : "no tengo suficientes notas."))
                        .withActions(ReplyAction.of("review", "Ver ficha de " + Grades.firstName(s.student().name()), "studentId", String.valueOf(s.student().id())), simulator));
            }
            List<GradeProjection> atRisk = c.projections().values().stream()
                    .filter(p -> p.status() == ProjectionStatus.HARD || p.status() == ProjectionStatus.IMPOSSIBLE)
                    .sorted(Comparator.comparing((GradeProjection p) -> p.needed() == null ? 9 : p.needed()).reversed()).toList();
            if (atRisk.isEmpty()) return Optional.of(AssistantReply.say("passing-grade", "Con las notas actuales, todos pueden llegar a 3,0 sin necesitar más de 3,5 en lo que falta.").withActions(simulator));
            List<ReplyItem> items = atRisk.stream().limit(6).map(p -> c.analysis().student(p.studentId())
                    .map(s -> new ReplyItem(p.studentId(), s.student().name(), p.needed() == null ? "—" : Grades.format(p.needed()), p.explanation(), "low")).orElse(null))
                    .filter(java.util.Objects::nonNull).toList();
            return Optional.of(AssistantReply.say("passing-grade", Grades.plural(atRisk.size(), "estudiante está", "estudiantes están") + " en riesgo de perder: necesitan más de 3,5 en lo que falta.")
                    .withItems(items).withActions(simulator));
        }
    }

    static final class FallingBehind implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("bajando", "bajado", "empeor", "cayendo", "riesgo", "preocup", "alerta")) return Optional.empty();
            ReplyAction trends = ReplyAction.of("navigate", "Ver tendencias", "route", "seguimiento", "vista", "tendencia");
            List<StudentPerformance> down = c.analysis().students().stream().filter(p -> p.direction() == Direction.DOWN)
                    .sorted(Comparator.comparingDouble(StudentPerformance::delta)).toList();
            if (down.isEmpty()) {
                boolean twoPeriods = c.analysis().periods().size() >= 2;
                return Optional.of(AssistantReply.say("falling-behind", twoPeriods ? "Buenas noticias: nadie ha bajado más de 0,3 entre cortes." : "Necesito notas de al menos dos cortes para ver tendencias.").withActions(trends));
            }
            return Optional.of(AssistantReply.say("falling-behind", Grades.plural(down.size(), "estudiante viene", "estudiantes vienen") + " bajando. " + Grades.firstName(down.get(0).student().name()) + " es quien más bajó.")
                    .withItems(down.stream().limit(6).map(p -> item(p, "−" + Grades.format(Math.abs(p.delta())), path(p))).toList()).withActions(trends));
        }
    }

    static final class Improving implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("subiendo", "mejorando", "mejoro", "mejorado", "progreso")) return Optional.empty();
            List<StudentPerformance> up = c.analysis().students().stream().filter(p -> p.direction() == Direction.UP)
                    .sorted(Comparator.comparingDouble(StudentPerformance::delta).reversed()).toList();
            if (up.isEmpty()) return Optional.of(AssistantReply.say("improving", "Por ahora nadie ha subido más de 0,3 entre cortes."));
            return Optional.of(AssistantReply.say("improving", Grades.plural(up.size(), "estudiante viene", "estudiantes vienen") + " mejorando. Vale la pena reconocérselo.")
                    .withItems(up.stream().limit(6).map(p -> item(p, "+" + Grades.format(p.delta()), path(p))).toList()));
        }
    }

    static final class BestStudents implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("mejor", "mejores", "destacad", "top", "quien va bien", "quienes van bien")) return Optional.empty();
            List<StudentPerformance> best = c.analysis().students().stream().filter(StudentPerformance::hasGrades)
                    .sorted(Comparator.comparingDouble(StudentPerformance::average).reversed()).limit(5).toList();
            if (best.isEmpty()) return Optional.empty();
            return Optional.of(AssistantReply.say("best", Grades.firstName(best.get(0).student().name()) + " lleva el mejor promedio con " + Grades.format(best.get(0).average()) + ".")
                    .withItems(best.stream().map(p -> item(p, Grades.format(p.average()), p.level().label())).toList())
                    .withActions(workshop("Crear un reto", "workshop", Level.HIGH, null, null)));
        }
    }

    static final class StudentStatus implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            StudentPerformance s = q.student();
            if (s == null || q.has("quienes", "cuantos")) return Optional.empty();
            String id = String.valueOf(s.student().id());
            StringBuilder text = new StringBuilder();
            if (!s.hasGrades()) text.append(s.student().name()).append(" todavía no tiene notas.");
            else {
                text.append(s.student().name()).append(" tiene ").append(Grades.format(s.average())).append(" de promedio en ")
                        .append(Grades.plural(s.gradeCount(), "nota", "notas")).append(": ").append(s.level().label().toLowerCase()).append('.');
                if (s.direction() == Direction.DOWN) text.append(" Viene bajando (").append(path(s)).append(").");
                if (s.direction() == Direction.UP) text.append(" Viene mejorando.");
                if (s.weakest() != null) text.append(" Su tema más difícil es ").append(s.weakest().name()).append(", con ").append(Grades.format(s.weakest().average())).append('.');
                GradeProjection p = c.projections().get(s.student().id());
                if (p != null && p.status() != ProjectionStatus.PASSED && p.status() != ProjectionStatus.NO_DATA) text.append(' ').append(p.explanation());
            }
            Level level = s.level() == Level.NO_DATA ? Level.MEDIUM : s.level();
            return Optional.of(AssistantReply.say("student-status", text.toString()).withActions(
                    ReplyAction.of("review", "Ver ficha", "studentId", id),
                    workshop("Taller para este estudiante", "workshop", level, s.weakest() == null ? null : s.weakest().name(), s),
                    ReplyAction.of("message", "Escribirle", "studentId", id)));
        }
    }

    static final class Averages implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("promedio", "media", "nota del curso", "como van")) return Optional.empty();
            SubjectAverage subject = q.subject();
            if (subject != null) {
                return Optional.of(AssistantReply.say("average", "En " + subject.name() + " el promedio es " + Grades.format(subject.average())
                        + (subject.studentsBelowPassing() > 0 ? " y " + Grades.plural(subject.studentsBelowPassing(), "estudiante está", "estudiantes están") + " por debajo de 3,0" : "") + ".")
                        .withActions(workshop("Taller de " + subject.name(), "workshop", Level.LOW, subject.name(), null)));
            }
            CourseSummary s = c.analysis().summary();
            List<SubjectAverage> subjects = c.analysis().subjects();
            String extra = subjects.isEmpty() ? "" : " El tema más bajo es " + subjects.get(0).name() + " (" + Grades.format(subjects.get(0).average()) + ") y el más alto "
                    + subjects.get(subjects.size() - 1).name() + " (" + Grades.format(subjects.get(subjects.size() - 1).average()) + ").";
            return Optional.of(AssistantReply.say("average", "El promedio de " + c.analysis().courseName() + " es " + Grades.format(s.average()) + " con " + Grades.plural(s.gradeCount(), "nota", "notas") + "." + extra));
        }
    }

    static final class WhoNeedsHelp implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("quien", "quienes", "cuales", "cuantos", "lista") && q.level() == null && q.subject() == null) return Optional.empty();
            SubjectAverage subject = q.subject();
            if (subject != null) {
                record Hit(StudentPerformance p, double avg) { }
                List<Hit> hits = c.analysis().students().stream()
                        .map(p -> p.subjects().stream().filter(x -> x.subjectId() == subject.subjectId()).findFirst().map(x -> new Hit(p, x.average())).orElse(null))
                        .filter(h -> h != null && Grades.round1(h.avg()) < Grades.PASSING).sorted(Comparator.comparingDouble(Hit::avg)).toList();
                String text = hits.isEmpty()
                        ? "En " + subject.name() + " nadie está por debajo de 3,0. Promedio del tema: " + Grades.format(subject.average()) + "."
                        : "En " + subject.name() + ", " + Grades.plural(hits.size(), "estudiante está", "estudiantes están") + " por debajo de 3,0. Promedio del tema: " + Grades.format(subject.average()) + ".";
                AssistantReply reply = AssistantReply.say("who-needs-help", text)
                        .withItems(hits.stream().limit(8).map(h -> new ReplyItem(h.p().student().id(), h.p().student().name(), Grades.format(h.avg()), "promedio general " + Grades.format(h.p().average()), "low")).toList());
                return Optional.of(hits.isEmpty() ? reply : reply.withActions(workshop("Taller de " + subject.name(), "workshop", Level.LOW, subject.name(), null)));
            }
            Level level = q.level() == null ? Level.LOW : q.level();
            List<StudentPerformance> list = c.analysis().students().stream().filter(p -> p.hasGrades() && p.level() == level)
                    .sorted(Comparator.comparingDouble(StudentPerformance::average)).toList();
            String many = switch (level) { case LOW -> "necesitan refuerzo"; case MEDIUM -> "están en seguimiento"; default -> "van con buen progreso"; };
            String one = switch (level) { case LOW -> "necesita refuerzo"; case MEDIUM -> "está en seguimiento"; default -> "va con buen progreso"; };
            if (list.isEmpty()) return Optional.of(AssistantReply.say("who-needs-help", "Nadie " + one + " en este momento."));
            String text = list.size() == 1 ? "Solo " + list.get(0).student().name() + " " + one + "." : list.size() + " estudiantes " + many + ".";
            AssistantReply reply = AssistantReply.say("who-needs-help", text)
                    .withItems(list.stream().limit(8).map(p -> item(p, Grades.format(p.average()), Grades.plural(p.gradeCount(), "nota", "notas"))).toList());
            String topic = c.analysis().weakestSubject().map(SubjectAverage::name).orElse(null);
            return Optional.of(level == Level.LOW ? reply.withActions(workshop("Preparar refuerzo", "workshop", Level.LOW, topic, null)) : reply);
        }
    }

    static final class Navigation implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            String[][] routes = {
                    {"equipo|grupos de trabajo", "Abro el armador de equipos.", "equipos", null},
                    {"importa|subir notas|cargar notas|sube las notas|subir la lista|cargar la lista|estudiantes", "Abro Estudiantes y notas para que importes el Excel.", "estudiantes", null},
                    {"material|archivo", "Abro Talleres: ahí puedes subir el material del curso.", "talleres", null},
                    {"simulador|proyeccion", "Abro el simulador de notas.", "seguimiento", "simulador"},
                    {"seguimiento|alertas", "Abro el seguimiento.", "seguimiento", null},
                    {"ajustes|configuracion", "Abro los ajustes.", "ajustes", null},
                    {"inicio|principal", "Volvemos al inicio.", "inicio", null},
            };
            for (String[] route : routes) {
                if (q.has(route[0].split("\\|"))) {
                    return Optional.of(AssistantReply.say("navigate", route[1]).andRun(ReplyAction.of("navigate", route[2], "route", route[2], "vista", route[3])));
                }
            }
            return Optional.empty();
        }
    }

    static final class MissingGrades implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("sin notas", "no tiene notas", "no tienen notas", "falta nota", "faltan notas", "sin calificacion")) return Optional.empty();
            List<StudentPerformance> missing = c.analysis().students().stream().filter(p -> !p.hasGrades()).toList();
            if (missing.isEmpty()) return Optional.of(AssistantReply.say("missing-grades", "Todos tus estudiantes tienen al menos una nota."));
            return Optional.of(AssistantReply.say("missing-grades", Grades.plural(missing.size(), "estudiante no tiene", "estudiantes no tienen") + " notas todavía. Revisa si faltan en el archivo.")
                    .withItems(missing.stream().limit(8).map(p -> item(p, "—", "sin notas")).toList())
                    .withActions(ReplyAction.of("navigate", "Ver quiénes", "route", "estudiantes", "filtro", "sin-notas")));
        }
    }

    static final class CourseCounts implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("cuantos estudiantes", "cuantos alumnos", "cuantos son", "tamano del curso", "cuantas notas")) return Optional.empty();
            CourseSummary s = c.analysis().summary();
            return Optional.of(AssistantReply.say("course-counts", c.analysis().courseName() + " tiene " + Grades.plural(s.students(), "estudiante", "estudiantes")
                    + " y " + Grades.plural(s.gradeCount(), "nota cargada", "notas cargadas") + ": " + s.low() + " en refuerzo, " + s.medium() + " en seguimiento y "
                    + s.high() + " con buen progreso" + (s.ungraded() > 0 ? "; " + s.ungraded() + " sin notas" : "") + "."));
        }
    }

    static final class SubjectExtremes implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("tema", "temas", "materia")) return Optional.empty();
            boolean easiest = q.has("facil", "mejor", "mas alto");
            boolean hardest = q.has("dificil", "cuesta", "peor", "mas bajo", "flojo");
            List<SubjectAverage> subjects = c.analysis().subjects();
            if ((!easiest && !hardest) || subjects.isEmpty()) return Optional.empty();
            SubjectAverage pick = easiest ? subjects.get(subjects.size() - 1) : subjects.get(0);
            String text = (easiest ? "El tema con mejor promedio es " : "El tema que más cuesta es ") + pick.name() + ", con " + Grades.format(pick.average())
                    + (pick.studentsBelowPassing() > 0 ? "; " + Grades.plural(pick.studentsBelowPassing(), "estudiante está", "estudiantes están") + " por debajo de 3,0." : ".");
            AssistantReply reply = AssistantReply.say("subject-extremes", text);
            return Optional.of(easiest ? reply : reply.withActions(workshop("Taller de " + pick.name(), "workshop", Level.LOW, pick.name(), null)));
        }
    }

    static final class PeriodComparison implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            if (!q.has("compara", "comparacion", "entre cortes", "por corte", "cada corte")) return Optional.empty();
            List<String> periods = c.analysis().periods();
            if (periods.size() < 2) return Optional.of(AssistantReply.say("period-comparison", "Por ahora hay notas de un solo periodo; necesito dos para comparar."));
            List<String> parts = new java.util.ArrayList<>();
            for (String period : periods) {
                List<Double> values = c.analysis().students().stream().flatMap(p -> p.periods().stream()).filter(x -> x.period().equals(period)).map(PeriodAverage::average).toList();
                if (!values.isEmpty()) parts.add(period + ": " + Grades.format(Grades.average(values)));
            }
            return Optional.of(AssistantReply.say("period-comparison", "Promedio del curso por corte. " + String.join(", ", parts) + ".")
                    .withActions(ReplyAction.of("navigate", "Ver tendencias", "route", "seguimiento", "vista", "tendencia")));
        }
    }

    static final class Fallback implements IntentHandler {
        public Optional<AssistantReply> handle(AssistantQuery q, AssistantContext c) {
            return Optional.of(AssistantReply.say("fallback", "No estoy seguro de haberte entendido. Prueba con algo como: “¿quién viene bajando?” o “crea un quiz de funciones”."));
        }
    }
}
