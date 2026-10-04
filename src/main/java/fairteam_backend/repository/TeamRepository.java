package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.Team;

public interface TeamRepository extends JpaRepository<Team, Long> {
}