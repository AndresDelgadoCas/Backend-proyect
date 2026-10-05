package fairteam_backend.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import fairteam_backend.entity.StudentSkill;
import fairteam_backend.repository.StudentSkillRepository;

@Service
public class StudentSkillService {

    private final StudentSkillRepository studentSkillRepository;

    public StudentSkillService(StudentSkillRepository studentSkillRepository) {
        this.studentSkillRepository = studentSkillRepository;
    }

    public List<StudentSkill> findAll() {
        return studentSkillRepository.findAll();
    }

    public Optional<StudentSkill> findById(
            fairteam_backend.entity.StudentSkillId id) {
        return studentSkillRepository.findById(id);
    }

    public StudentSkill save(StudentSkill studentSkill) {
        return studentSkillRepository.save(studentSkill);
    }

    public void delete(fairteam_backend.entity.StudentSkillId id) {
        if (!studentSkillRepository.existsById(id)) {
            throw new RuntimeException("Student skill not found");
        }

        studentSkillRepository.deleteById(id);
    }
}