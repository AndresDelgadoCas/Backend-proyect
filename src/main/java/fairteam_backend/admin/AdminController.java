package fairteam_backend.admin;

import java.util.List;
import java.util.Map;

import jakarta.validation.constraints.Size;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Super administrator API. Requires ROLE_ADMIN (security chain) and an admin row in the database (tenant filter). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;

    public AdminController(AdminService admin) { this.admin = admin; }

    @GetMapping("/overview")
    public AdminService.Overview overview() { return admin.overview(); }

    @GetMapping("/accounts")
    public List<AdminService.AccountRow> accounts() { return admin.accounts(); }

    @GetMapping("/accounts/{id}")
    public AdminService.AccountDetail detail(@PathVariable long id) { return admin.detail(id); }

    @PatchMapping("/accounts/{id}")
    public AdminService.AccountRow update(@AuthenticationPrincipal Jwt jwt, @PathVariable long id,
            @RequestBody UpdateRequest request) {
        return admin.update(Long.parseLong(jwt.getSubject()), id, request.active(), request.role());
    }

    @PostMapping("/accounts/{id}/reset-password")
    public AdminService.PasswordReset resetPassword(@AuthenticationPrincipal Jwt jwt, @PathVariable long id) {
        return admin.resetPassword(Long.parseLong(jwt.getSubject()), id);
    }

    @GetMapping("/audit")
    public List<AdminService.AuditRow> audit(@RequestParam(defaultValue = "50") int limit) { return admin.audit(limit); }

    @GetMapping("/system")
    public Map<String, Object> system() { return admin.system(); }

    public record UpdateRequest(Boolean active, @Size(max = 20) String role) { }
}
