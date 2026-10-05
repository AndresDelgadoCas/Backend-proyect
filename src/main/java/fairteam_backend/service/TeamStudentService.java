package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.TeamStudent;
import fairteam_backend.entity.TeamStudentId;
import fairteam_backend.repository.TeamStudentRepository;

@Service
public class TeamStudentService {

    private final TeamStudentRepository repository;

    public TeamStudentService(TeamStudentRepository repository) {
        this.repository = repository;
    }

    public List<TeamStudent> findAll() {
        return repository.findAll();
    }

    public Optional<TeamStudent> findById(TeamStudentId id) {
        return repository.findById(id);
    }

    public TeamStudent save(TeamStudent teamStudent) {
        return repository.save(teamStudent);
    }

    public void delete(TeamStudentId id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Team student relation not found");
        }

        repository.deleteById(id);
    }
}