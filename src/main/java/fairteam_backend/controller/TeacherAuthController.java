package fairteam_backend.controller;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fairteam_backend.service.TeacherAuthService;

@RestController
@RequestMapping("/api/auth")
public class TeacherAuthController {
    private final TeacherAuthService auth;

    public TeacherAuthController(TeacherAuthService auth) { this.auth = auth; }

    @PostMapping("/register")
    public ResponseEntity<TeacherAuthService.AuthResult> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(auth.register(
                request.name(), request.email(), request.password()));
    }

    @PostMapping("/login")
    public TeacherAuthService.AuthResult login(@Valid @RequestBody LoginRequest request) {
        return auth.login(request.email(), request.password());
    }

    @GetMapping("/me")
    public Map<String, String> me(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return Map.of("id", jwt.getSubject(), "email", jwt.getClaimAsString("email"),
                "name", jwt.getClaimAsString("name"), "role", "TEACHER");
    }

    public record RegisterRequest(@NotBlank @Size(max = 120) String name,
            @NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(min = 10, max = 72) String password) { }

    public record LoginRequest(@NotBlank @Email @Size(max = 180) String email,
            @NotBlank @Size(max = 72) String password) { }
}
