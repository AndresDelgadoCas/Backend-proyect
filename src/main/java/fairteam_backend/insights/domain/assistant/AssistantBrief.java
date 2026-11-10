package fairteam_backend.insights.domain.assistant;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fairteam_backend.insights.domain.Grades;
import fairteam_backend.insights.domain.Level;
import fairteam_backend.insights.domain.StudentPerformance;
import fairteam_backend.insights.domain.SubjectAverage;

/**
 * Bridge between Búho and the AI interpreter. Builds the request with codes instead of identities (students
 * E1…, saved activities A1…, other courses C1…; no names, emails or individual grades leave the backend) and
 * turns the {@link Interpretation} back into an {@link AssistantReply} the app runs.
 */
public final class AssistantBrief {
    private static final Pattern CODE = Pattern.compile("(?<![\\p{L}\\d])([EAC])(\\d{1,3})(?![\\p{L}\\d])");
    private static final Pattern REFERENCE = Pattern.compile(
            "(^| )(ella|el mismo|la misma|lo mismo|ese|esa|eso|esos|esas|este|esta|otro|otra|tambien|anterior|igual|ahora|entonces|y para|y de|y en|le|les|al ultimo|a ese|a esa)( |$)");
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM", Locale.of("es"));

    /** The request as the interpreter receives it. */
    public record Request(String message, List<Line> history, String course, double scale, String summary,
            List<Topic> topics, List<Student> students, List<Activity> activities, List<Course> courses, String manual) { }

    public record Line(String role, String text) { }

    public record Topic(String name, Double average) { }

    public record Student(String code, String level, String weakest) { }

    public record Activity(String code, String kind, String title, String topic, String created) { }

    public record Course(String code, String name) { }

    private final AssistantContext context;
    private final Map<String, StudentPerformance> students = new LinkedHashMap<>();
    private final Map<String, ActivityRef> activities = new LinkedHashMap<>();
    private final Map<String, CourseRef> courses = new LinkedHashMap<>();
    private final List<Map.Entry<Pattern, String>> names = new ArrayList<>();

    public AssistantBrief(AssistantContext context) {
        this.context = context;
        int i = 0;
        for (StudentPerformance p : context.analysis().students()) students.put("E" + ++i, p);
        i = 0;
        for (ActivityRef a : context.activities()) activities.put("A" + ++i, a);
        i = 0;
        for (CourseRef c : context.courses()) if (c.id() != context.analysis().courseId()) courses.put("C" + ++i, c);
        indexNames();
    }

    /** Long requests, references to earlier turns ("hazlo para ella") and anything the rules did not get go to the AI. */
    public static boolean worthInterpreting(String question, AssistantReply ruleReply, AssistantContext context) {
        if (ruleReply == null || "fallback".equals(ruleReply.intent())) return true;
        if (List.of("follow-up", "empty", "small-talk", "briefing", "how-to", "help").contains(ruleReply.intent())) return false;
        String text = AssistantQuery.normalize(question);
        if (text.split(" ").length >= 12) return true;
        return !context.history().isEmpty() && REFERENCE.matcher(text).find();
    }

    public Request request(String message) {
        List<Line> history = context.history().stream().skip(Math.max(0, context.history().size() - 8))
                .map(t -> new Line(t.fromBuho() ? "buho" : "teacher", encode(t.text() + offeredNote(t)))).toList();
        var s = context.analysis().summary();
        String summary = s.students() + " estudiantes; " + s.graded() + " con notas: " + s.low() + " en refuerzo, " + s.medium()
                + " en seguimiento, " + s.high() + " con buen progreso; " + s.fallingBehind() + " vienen bajando. Material subido: " + context.materials() + ".";
        List<Topic> topics = context.analysis().subjects().stream().map(t -> new Topic(t.name(), Grades.round1(t.average()))).toList();
        List<Student> studentCodes = students.entrySet().stream().map(e -> new Student(e.getKey(), levelWord(e.getValue().level()),
                e.getValue().weakest() == null ? "" : e.getValue().weakest().name())).toList();
        List<Activity> activityCodes = activities.entrySet().stream().map(e -> new Activity(e.getKey(), e.getValue().kind(), e.getValue().title(),
                e.getValue().topic() == null ? "" : e.getValue().topic(), e.getValue().created() == null ? "" : DAY.format(e.getValue().created()))).toList();
        List<Course> courseCodes = courses.entrySet().stream().map(e -> new Course(e.getKey(), e.getValue().name())).toList();
        return new Request(encode(message), history, context.analysis().courseName(), 5.0, summary, topics, studentCodes, activityCodes, courseCodes, AppManual.text());
    }

    /**
     * The reply for what the interpreter understood. {@code rules} answers questions about the course
     * (rewritten by the interpreter); empty when the interpretation cannot be used.
     */
    public Optional<AssistantReply> reply(Interpretation i, Function<String, AssistantReply> rules) {
        StudentPerformance student = students.get(i.student());
        String text = decode(i.reply());
        return Optional.ofNullable(switch (i.action()) {
            case "ask_course" -> {
                AssistantReply answer = rules.apply(decode(i.question().isBlank() ? i.reply() : i.question()));
                if (!"fallback".equals(answer.intent())) yield answer;
                yield text.isBlank() ? null : AssistantReply.say("ai-answer", text);
            }
            case "create_activity" -> createActivity(i, student, text);
            case "create_rubric" -> createRubric(i, text);
            case "draft_message" -> student == null ? null : AssistantReply.say("ai-message", orElse(text, "Te dejo el mensaje para " + first(student) + "; revísalo antes de enviarlo."))
                    .andRun(ReplyAction.of("message", "Escribir a " + first(student), "studentId", id(student)));
            case "review_student" -> student == null ? null : AssistantReply.say("ai-review", orElse(text, "Te abro la ficha de " + first(student) + "."))
                    .andRun(ReplyAction.of("review", "Ver a " + first(student), "studentId", id(student)));
            case "open_section" -> AssistantReply.say("ai-navigate", orElse(text, "Te llevo allá."))
                    .andRun(ReplyAction.of("navigate", "Abrir", "route", i.section().isBlank() ? "inicio" : i.section()));
            case "switch_course" -> Optional.ofNullable(courses.get(i.course())).map(c -> AssistantReply.say("ai-course", orElse(text, "Abro " + c.name() + "."))
                    .andRun(ReplyAction.of("course", "Abrir " + c.name(), "courseId", String.valueOf(c.id())))).orElse(null);
            case "report" -> AssistantReply.say("ai-report", orElse(text, "Genero el informe del curso.")).andRun(ReplyAction.of("report", "Informe del curso"));
            case "how_to" -> AppManual.entries().stream().filter(e -> e.id().equals(i.topic())).findFirst()
                    .map(e -> HowToHandler.reply(e, null, context)).orElse(text.isBlank() ? null : AssistantReply.say("ai-answer", text));
            case "answer", "clarify" -> text.isBlank() ? null : AssistantReply.say("ai-" + i.action(), text);
            default -> null;
        });
    }

    private AssistantReply createActivity(Interpretation i, StudentPerformance student, String text) {
        String mode = switch (i.kind()) { case "quiz" -> "quiz"; case "custom" -> "custom"; default -> "workshop"; };
        Level level = switch (i.level()) {
            case "LOW" -> Level.LOW; case "HIGH" -> Level.HIGH; case "MEDIUM" -> Level.MEDIUM;
            default -> student != null && student.level() != Level.NO_DATA ? student.level() : context.analysis().summary().low() > 0 ? Level.LOW : Level.MEDIUM;
        };
        String topic = !i.topic().isBlank() ? decode(i.topic())
                : student != null && student.weakest() != null ? student.weakest().name()
                : context.analysis().weakestSubject().map(SubjectAverage::name).orElse(null);
        String instructions = decode(i.instructions());
        ReplyAction action = Handlers.workshop("Ver en Talleres", mode, level, topic, student).with("auto", "true")
                .with("prompt", instructions.isBlank() ? null : instructions);
        String what = switch (mode) { case "quiz" -> "un quiz"; case "custom" -> "la actividad que pediste"; default -> "un taller"; };
        String fallback = "Listo, estoy creando " + what + (topic != null ? " sobre " + topic : "") + (student != null ? " para " + first(student) : "")
                + ". Lo verás en Talleres en unos segundos y queda guardado.";
        return AssistantReply.say("create-activity", orElse(text, fallback)).andRun(action);
    }

    private AssistantReply createRubric(Interpretation i, String text) {
        ActivityRef activity = activities.get(i.activity());
        if (activity == null) activity = CreateRubricHandler.pick(context.activities(), "workshop", i.topic().isBlank() ? null : decode(i.topic())).orElse(null);
        if (activity == null) {
            return AssistantReply.say("create-rubric", "Todavía no tienes talleres ni quizzes guardados en este curso. ¿Creo uno primero y luego armo la rúbrica a partir de él?")
                    .withActions(ReplyAction.of("workshop", "Crear el taller primero", "mode", "workshop", "level", "MEDIUM", "topic", i.topic().isBlank() ? null : decode(i.topic())));
        }
        String instructions = decode(i.instructions());
        ReplyAction action = ReplyAction.of("rubric", "Ver la rúbrica", "activityId", String.valueOf(activity.id()), "target", activity.kind(),
                "prompt", instructions.isBlank() ? null : instructions);
        return AssistantReply.say("create-rubric", orElse(text, "Tomo \"" + activity.title() + "\", leo sus actividades y armo la rúbrica. Queda guardada y enlazada; te la abro para revisarla."))
                .andRun(action);
    }

    /** Teacher-facing text with codes turned back into names and titles. */
    String decode(String text) {
        if (text == null || text.isBlank()) return "";
        Matcher m = CODE.matcher(text);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String code = m.group(1) + m.group(2);
            String name = switch (m.group(1)) {
                case "E" -> Optional.ofNullable(students.get(code)).map(p -> p.student().name()).orElse(code);
                case "A" -> Optional.ofNullable(activities.get(code)).map(a -> "\"" + a.title() + "\"").orElse(code);
                default -> Optional.ofNullable(courses.get(code)).map(CourseRef::name).orElse(code);
            };
            m.appendReplacement(out, Matcher.quoteReplacement(name));
        }
        m.appendTail(out);
        return out.toString();
    }

    /** Text with each student's name (full, first + last, or a first name nobody else has) replaced by their code. */
    String encode(String text) {
        String out = text == null ? "" : text;
        for (var entry : names) out = entry.getKey().matcher(out).replaceAll(entry.getValue());
        return out;
    }

    private void indexNames() {
        Map<String, Integer> firstNames = new LinkedHashMap<>();
        for (StudentPerformance p : students.values()) firstNames.merge(AssistantQuery.normalize(Grades.firstName(p.student().name())), 1, Integer::sum);
        List<Map.Entry<String, String>> variants = new ArrayList<>();
        students.forEach((code, p) -> {
            String[] parts = p.student().name().trim().split("\\s+");
            variants.add(Map.entry(p.student().name().trim(), code));
            if (parts.length >= 3) variants.add(Map.entry(parts[0] + " " + parts[parts.length >= 4 ? 2 : 1], code));
            if (parts.length >= 2) variants.add(Map.entry(parts[0] + " " + parts[1], code));
            if (parts[0].length() >= 3 && firstNames.getOrDefault(AssistantQuery.normalize(parts[0]), 0) == 1) variants.add(Map.entry(parts[0], code));
        });
        variants.sort(Comparator.comparingInt((Map.Entry<String, String> e) -> e.getKey().length()).reversed());
        for (var v : variants) names.add(Map.entry(Pattern.compile("(?<![\\p{L}])" + loose(v.getKey()) + "(?![\\p{L}])", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE), v.getValue()));
    }

    /** A name pattern that ignores accents and extra spaces: "Sofía Ríos" also matches "sofia rios". */
    private static String loose(String name) {
        StringBuilder out = new StringBuilder();
        for (char ch : name.toCharArray()) {
            String plain = AssistantQuery.normalize(String.valueOf(ch));
            out.append(switch (plain) {
                case "a" -> "[aáàäâ]"; case "e" -> "[eéèëê]"; case "i" -> "[iíìïî]"; case "o" -> "[oóòöô]"; case "u" -> "[uúùüû]"; case "n" -> "[nñ]";
                case "" -> Character.isWhitespace(ch) ? "\\s+" : Pattern.quote(String.valueOf(ch));
                default -> Pattern.quote(String.valueOf(ch));
            });
        }
        return out.toString();
    }

    private static String offeredNote(Turn t) {
        if (!t.fromBuho()) return "";
        StringBuilder note = new StringBuilder();
        if (t.ran() != null) note.append(" [Búho ya hizo: ").append(describe(t.ran())).append("]");
        if (!t.offered().isEmpty()) note.append(" [Búho ofreció: ").append(String.join("; ", t.offered().stream().map(AssistantBrief::describe).toList())).append("]");
        return note.toString();
    }

    private static String describe(ReplyAction a) {
        String topic = a.params().getOrDefault("topic", "");
        String what = switch (a.kind()) {
            case "workshop" -> "quiz".equals(a.params().get("mode")) ? "crear un quiz" : "crear un taller";
            case "rubric" -> "crear una rúbrica";
            case "message" -> "escribir un mensaje";
            case "review" -> "abrir la ficha de un estudiante";
            case "navigate" -> "abrir " + a.params().getOrDefault("route", "una sección");
            default -> a.kind();
        };
        return what + (topic.isBlank() ? "" : " sobre " + topic);
    }

    private static String levelWord(Level level) {
        return switch (level) { case LOW -> "refuerzo"; case MEDIUM -> "seguimiento"; case HIGH -> "buen progreso"; default -> "sin notas"; };
    }

    private static String first(StudentPerformance p) { return Grades.firstName(p.student().name()); }

    private static String id(StudentPerformance p) { return String.valueOf(p.student().id()); }

    private static String orElse(String text, String fallback) { return text == null || text.isBlank() ? fallback : text; }
}
