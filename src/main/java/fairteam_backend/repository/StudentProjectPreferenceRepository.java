package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.StudentProjectPreference;

public interface StudentProjectPreferenceRepository
        extends JpaRepository<StudentProjectPreference, Long> {
}