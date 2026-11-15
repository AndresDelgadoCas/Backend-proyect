package fairteam_backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import fairteam_backend.entity.TeacherUser;
import fairteam_backend.repository.TeacherUserRepository;

class AdminServiceTest {
    private final TeacherUserRepository users = mock(TeacherUserRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private AdminService service;

    @BeforeEach
    void setUp() {
        service = new AdminService(users, jdbc, "http://localhost:8000");
    }

    private TeacherUser user(long id, String role, boolean active) {
        TeacherUser u = new TeacherUser();
        u.setId(id);
        u.setEmail("u" + id + "@x.test");
        u.setRole(role);
        u.setActive(active);
        u.setPasswordHash("old");
        return u;
    }

    @Test
    void anAdminCannotDeactivateThemselves() {
        when(users.findById(1L)).thenReturn(Optional.of(user(1, "ADMIN", true)));
        assertThatThrownBy(() -> service.update(1, 1, false, null))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.CONFLICT));
        verify(users, never()).save(any());
    }

    @Test
    void theLastActiveAdminCannotBeDemoted() {
        when(users.findById(2L)).thenReturn(Optional.of(user(2, "ADMIN", true)));
        when(users.countByRoleAndActive("ADMIN", true)).thenReturn(1L);
        assertThatThrownBy(() -> service.update(1, 2, null, "TEACHER"))
                .isInstanceOf(ResponseStatusException.class).hasMessageContaining("al menos un administrador");
    }

    @Test
    void rejectsUnknownRolesAndEmptyChanges() {
        when(users.findById(3L)).thenReturn(Optional.of(user(3, "TEACHER", true)));
        assertThatThrownBy(() -> service.update(1, 3, null, "ROOT")).isInstanceOf(ResponseStatusException.class);
        assertThatThrownBy(() -> service.update(1, 3, null, null)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void deactivatingATeacherSavesAndWritesTheAuditLog() {
        TeacherUser teacher = user(3, "TEACHER", true);
        when(users.findById(3L)).thenReturn(Optional.of(teacher));
        when(jdbc.query(any(String.class), any(org.springframework.jdbc.core.RowMapper.class), any(Object[].class)))
                .thenReturn(java.util.List.of(new AdminService.AccountRow(3, "T", "u3@x.test", "TEACHER", false,
                        null, null, 0, 0, 0, 0)));
        service.update(1, 3, false, null);
        assertThat(teacher.getActive()).isFalse();
        verify(users).saveAndFlush(teacher);
        verify(jdbc).update(any(String.class), any(Object[].class));
    }

    @Test
    void resetsATeacherPasswordButNeverAnotherAdmins() {
        TeacherUser teacher = user(3, "TEACHER", true);
        when(users.findById(3L)).thenReturn(Optional.of(teacher));
        when(users.findById(2L)).thenReturn(Optional.of(user(2, "ADMIN", true)));

        var reset = service.resetPassword(1, 3);

        assertThat(reset.temporaryPassword()).hasSize(16);
        assertThat(teacher.getPasswordHash()).isNotEqualTo("old").startsWith("$2");
        assertThatThrownBy(() -> service.resetPassword(1, 2)).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void unknownAccountsAreNotFound() {
        when(users.findById(9L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.resetPassword(1, 9L))
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND));
    }
}
