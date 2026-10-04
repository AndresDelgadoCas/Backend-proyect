package fairteam_backend.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class TeamStudentId implements Serializable {

    private Long teamId;
    private Long studentId;

    public TeamStudentId() {
    }

    public TeamStudentId(Long teamId, Long studentId) {
        this.teamId = teamId;
        this.studentId = studentId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TeamStudentId)) return false;

        TeamStudentId that = (TeamStudentId) o;

        return Objects.equals(teamId, that.teamId)
                && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamId, studentId);
    }
}