package fairteam_backend.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class GradeJsonTest {
    private final JsonMapper json = JsonMapper.builder().build();

    @Test
    void responsesCarryIdsButNotTheRelatedObjects() throws Exception {
        Student student = new Student();
        student.setId(7L);
        student.setName("Ana Ruiz");
        student.setEmail("ana@u.edu");
        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setGrade(new BigDecimal("4.20"));
        grade.setPeriod("Corte 1");

        String body = json.writeValueAsString(grade);
        assertThat(body).contains("\"studentId\":7").doesNotContain("ana@u.edu").doesNotContain("\"student\"");
    }

    @Test
    void requestsCanStillSetTheStudent() throws Exception {
        Grade grade = json.readValue("{\"student\":{\"id\":7},\"grade\":3.5,\"period\":\"Corte 1\"}", Grade.class);
        assertThat(grade.getStudentId()).isEqualTo(7L);
    }
}
