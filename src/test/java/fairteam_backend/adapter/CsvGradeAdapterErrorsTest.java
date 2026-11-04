package fairteam_backend.adapter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvGradeAdapterErrorsTest {
    private final GradeAdapter adapter = new CsvGradeAdapter();

    @Test
    void missingFilesAreReportedClearly() {
        assertThatThrownBy(() -> adapter.importGrades("no-existe.csv"))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("No se pudo leer");
    }

    @Test
    void invalidNumbersAreReportedClearly(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("notas.csv");
        Files.writeString(file, "id,studentId,subjectId,grade,period\n1,1,1,cuatro,Corte 1\n");
        assertThatThrownBy(() -> adapter.importGrades(file.toString()))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("valor o una columna");
    }

    @Test
    void missingColumnsAreReportedClearly(@TempDir Path dir) throws Exception {
        Path file = dir.resolve("notas.csv");
        Files.writeString(file, "id,studentId,grade\n1,1,4.0\n");
        assertThatThrownBy(() -> adapter.importGrades(file.toString())).isInstanceOf(IllegalArgumentException.class);
    }
}
