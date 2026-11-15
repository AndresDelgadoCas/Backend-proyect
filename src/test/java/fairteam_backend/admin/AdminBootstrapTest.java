package fairteam_backend.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.jdbc.core.JdbcTemplate;

import fairteam_backend.entity.TeacherUser;
import fairteam_backend.repository.TeacherUserRepository;

class AdminBootstrapTest {
    private final TeacherUserRepository users = mock(TeacherUserRepository.class);
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);

    private AdminBootstrap bootstrap(String email, String password, boolean reset) {
        return new AdminBootstrap(users, jdbc, email, password, "Admin", reset);
    }

    @Test
    void doesNothingWithoutConfiguration() {
        bootstrap("", "", false).run(null);
        verify(users, never()).save(any());
    }

    @Test
    void rejectsShortPasswords() {
        bootstrap("root@x.test", "short", false).run(null);
        verify(users, never()).save(any());
    }

    @Test
    void createsTheAdministratorWithAHashedPassword() {
        when(users.findByEmail("root@x.test")).thenReturn(Optional.empty());
        bootstrap("Root@X.test", "a-long-secret-123", false).run(null);
        ArgumentCaptor<TeacherUser> saved = ArgumentCaptor.forClass(TeacherUser.class);
        verify(users).save(saved.capture());
        assertThat(saved.getValue().getRole()).isEqualTo("ADMIN");
        assertThat(saved.getValue().getEmail()).isEqualTo("root@x.test");
        assertThat(saved.getValue().getPasswordHash()).startsWith("$2").doesNotContain("a-long-secret-123");
    }

    @Test
    void promotingAnExistingTeacherReplacesItsPassword() {
        TeacherUser teacher = new TeacherUser();
        teacher.setEmail("root@x.test");
        teacher.setRole("TEACHER");
        teacher.setPasswordHash("someone-elses");
        when(users.findByEmail("root@x.test")).thenReturn(Optional.of(teacher));
        bootstrap("root@x.test", "a-long-secret-123", false).run(null);
        assertThat(teacher.getRole()).isEqualTo("ADMIN");
        assertThat(teacher.getPasswordHash()).isNotEqualTo("someone-elses");
    }

    @Test
    void anExistingAdminKeepsItsPasswordUnlessResetIsRequested() {
        TeacherUser admin = new TeacherUser();
        admin.setEmail("root@x.test");
        admin.setRole("ADMIN");
        admin.setPasswordHash("mine");
        when(users.findByEmail("root@x.test")).thenReturn(Optional.of(admin));
        bootstrap("root@x.test", "a-long-secret-123", false).run(null);
        assertThat(admin.getPasswordHash()).isEqualTo("mine");
        bootstrap("root@x.test", "a-long-secret-123", true).run(null);
        assertThat(admin.getPasswordHash()).isNotEqualTo("mine");
    }
}
