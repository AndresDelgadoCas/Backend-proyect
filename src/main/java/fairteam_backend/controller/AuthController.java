package fairteam_backend.controller;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> currentTeacher(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        if (jwt == null || jwt.getClaimAsString("email") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of(
                "email", jwt.getClaimAsString("email"),
                "name", jwt.getClaimAsString("name") == null ? "Docente" : jwt.getClaimAsString("name"),
                "role", authentication.getAuthorities().stream().anyMatch(authority -> "ROLE_TEACHER".equals(authority.getAuthority())) ? "TEACHER" : "USER"));
    }
}
