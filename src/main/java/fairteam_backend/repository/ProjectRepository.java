package fairteam_backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import fairteam_backend.entity.Project;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}