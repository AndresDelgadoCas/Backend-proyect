package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.Skill;
import fairteam_backend.repository.SkillRepository;

@Service
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillService(SkillRepository skillRepository) {
        this.skillRepository = skillRepository;
    }

    public List<Skill> findAll() {
        return skillRepository.findAll();
    }

    public Optional<Skill> findById(Long id) {
        return skillRepository.findById(id);
    }

    public Skill save(Skill skill) {
        return skillRepository.save(skill);
    }

    public Skill update(Long id, Skill skill) {
        return skillRepository.findById(id)
                .map(existingSkill -> {
                    existingSkill.setName(skill.getName());
                    return skillRepository.save(existingSkill);
                })
                .orElseThrow(() -> new RuntimeException("Skill not found with id: " + id));
    }

    public void delete(Long id) {
        if (!skillRepository.existsById(id)) {
            throw new RuntimeException("Skill not found with id: " + id);
        }

        skillRepository.deleteById(id);
    }
}