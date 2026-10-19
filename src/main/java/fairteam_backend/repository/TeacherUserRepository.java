
package fairteam_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.TeacherUser;

public interface TeacherUserRepository
        extends JpaRepository<TeacherUser, Long> {

    Optional<TeacherUser> findByEmail(String email);
}
