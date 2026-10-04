package fairteam_backend.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class StudentSkillId implements Serializable {

    private Long studentId;
    private Long skillId;

    public StudentSkillId() {
    }

    public StudentSkillId(Long studentId, Long skillId) {
        this.studentId = studentId;
        this.skillId = skillId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getSkillId() {
        return skillId;
    }

    public void setSkillId(Long skillId) {
        this.skillId = skillId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StudentSkillId)) return false;

        StudentSkillId that = (StudentSkillId) o;

        return Objects.equals(studentId, that.studentId)
                && Objects.equals(skillId, that.skillId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentId, skillId);
    }
}