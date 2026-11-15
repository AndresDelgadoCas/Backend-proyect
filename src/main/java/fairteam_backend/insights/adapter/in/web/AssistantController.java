package fairteam_backend.insights.adapter.in.web;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.insights.application.port.in.AskAssistantUseCase;
import fairteam_backend.insights.domain.assistant.AssistantReply;
import fairteam_backend.insights.domain.assistant.Turn;

/** Búho for an account that has no course yet: it explains how FairTeam works and answers general questions. */
@RestController
@RequestMapping("/api/assistant")
public class AssistantController {
    private final AskAssistantUseCase assistant;

    public AssistantController(AskAssistantUseCase assistant) { this.assistant = assistant; }

    @PostMapping
    public AssistantReply ask(@RequestBody CourseInsightsController.QuestionRequest request, @AuthenticationPrincipal Jwt jwt) {
        String question = request.question() == null ? "" : request.question().trim();
        if (question.length() > 500) question = question.substring(0, 500);
        List<Turn> history = request.history() == null ? List.of()
                : request.history().subList(Math.max(0, request.history().size() - 10), request.history().size());
        String name = jwt == null ? null : jwt.getClaimAsString("name");
        return assistant.askWithoutCourse(question, name == null || name.isBlank() ? "profe" : name, history);
    }
}
