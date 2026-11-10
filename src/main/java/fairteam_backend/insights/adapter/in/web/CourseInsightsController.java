package fairteam_backend.insights.adapter.in.web;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.insights.application.CourseNotFoundException;
import fairteam_backend.insights.application.StudentNotFoundException;
import fairteam_backend.insights.application.port.in.AskAssistantUseCase;
import fairteam_backend.insights.application.port.in.CourseInsightsUseCase;
import fairteam_backend.insights.application.port.in.DraftFeedbackUseCase;
import fairteam_backend.insights.domain.assistant.AssistantReply;
import fairteam_backend.insights.domain.assistant.Turn;
import fairteam_backend.insights.domain.feedback.FeedbackTone;
import fairteam_backend.insights.domain.grading.GradingScheme;

/** Inbound HTTP adapter for the course assistant: analysis, simulator, feedback and Búho. */
@RestController
@RequestMapping("/api/courses/{courseId}")
public class CourseInsightsController {
    private final CourseInsightsUseCase insights;
    private final DraftFeedbackUseCase feedback;
    private final AskAssistantUseCase assistant;

    public CourseInsightsController(CourseInsightsUseCase insights, DraftFeedbackUseCase feedback, AskAssistantUseCase assistant) {
        this.insights = insights;
        this.feedback = feedback;
        this.assistant = assistant;
    }

    public record SchemeRequest(List<Integer> weights, Double passingGrade) { }

    /** The question plus the last turns of the conversation, so Búho remembers what was said and offered. */
    public record QuestionRequest(String question, List<Turn> history) { }

    @GetMapping("/insights")
    public CourseInsightsUseCase.CourseInsights insights(@PathVariable long courseId, @AuthenticationPrincipal Jwt jwt) {
        return insights.insights(courseId, teacher(jwt));
    }

    @GetMapping("/insights/report.csv")
    public ResponseEntity<String> csv(@PathVariable long courseId) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"seguimiento-curso-" + courseId + ".csv\"")
                .contentType(new MediaType("text", "csv", java.nio.charset.StandardCharsets.UTF_8))
                .body(insights.csvReport(courseId));
    }

    @PutMapping("/grading-scheme")
    public CourseInsightsUseCase.SchemeView updateScheme(@PathVariable long courseId, @RequestBody SchemeRequest request) {
        return insights.updateScheme(courseId, new GradingScheme(request.weights(), request.passingGrade() == null ? 3.0 : request.passingGrade()));
    }

    @GetMapping("/students/{studentId}/feedback")
    public DraftFeedbackUseCase.Feedback feedback(@PathVariable long courseId, @PathVariable long studentId,
            @RequestParam(defaultValue = "cercano") String tone, @AuthenticationPrincipal Jwt jwt) {
        return feedback.draft(courseId, studentId, FeedbackTone.parse(tone), teacher(jwt));
    }

    @PostMapping("/assistant")
    public AssistantReply ask(@PathVariable long courseId, @RequestBody QuestionRequest request, @AuthenticationPrincipal Jwt jwt) {
        String question = request.question() == null ? "" : request.question().trim();
        if (question.length() > 500) question = question.substring(0, 500);
        List<Turn> history = request.history() == null ? List.of()
                : request.history().subList(Math.max(0, request.history().size() - 10), request.history().size());
        return assistant.ask(courseId, question, teacher(jwt), history);
    }

    private static String teacher(Jwt jwt) {
        String name = jwt == null ? null : jwt.getClaimAsString("name");
        return name == null || name.isBlank() ? "profe" : name;
    }

    @ExceptionHandler({ CourseNotFoundException.class, StudentNotFoundException.class })
    ResponseEntity<Map<String, String>> notFound(RuntimeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<Map<String, String>> invalid(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("message", exception.getMessage()));
    }
}
