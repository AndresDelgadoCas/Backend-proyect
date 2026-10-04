package fairteam_backend.adapter;

import fairteam_backend.entity.Grade;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvGradeAdapterTest {

    @Test
    void shouldImportGradesFromCsv() {

        GradeAdapter adapter = new CsvGradeAdapter();

        List<Grade> grades = adapter.importGrades("test-data/grades.csv");

        assertEquals(4, grades.size());

        assertEquals(4.5, grades.get(0).getGrade());
        assertEquals(1L, grades.get(0).getStudentId());

        assertEquals(2.8, grades.get(1).getGrade());
        assertEquals(2L, grades.get(1).getStudentId());
    }
}