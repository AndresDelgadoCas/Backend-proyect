package fairteam_backend.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TeamCandidateTest {
    @Test
    void keepsTheScoresItWasBuiltWith() {
        TeamCandidate candidate = new TeamCandidate(7L, 4.2, 55, 80);
        assertThat(candidate.getStudentId()).isEqualTo(7L);
        assertThat(candidate.getAcademicScore()).isEqualTo(4.2);
        assertThat(candidate.getSkillScore()).isEqualTo(55);
        assertThat(candidate.getAvailabilityScore()).isEqualTo(80);
    }

    @Test
    void settersReplaceValues() {
        TeamCandidate candidate = new TeamCandidate();
        candidate.setStudentId(3L);
        candidate.setAcademicScore(2.5);
        candidate.setSkillScore(40);
        candidate.setAvailabilityScore(10);
        assertThat(candidate.getAcademicScore()).isEqualTo(2.5);
        assertThat(candidate.getSkillScore()).isEqualTo(40);
    }
}
