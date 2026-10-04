package fairteam_backend.adapter;

import fairteam_backend.entity.Grade;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CsvGradeAdapter implements GradeAdapter {

    @Override
    public List<Grade> importGrades(String filePath) {

        List<Grade> grades = new ArrayList<>();

        try {
            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader()
                    .setSkipHeaderRecord(true)
                    .build();

            try (var reader = Files.newBufferedReader(Path.of(filePath))) {

                Iterable<CSVRecord> records = format.parse(reader);

                for (CSVRecord record : records) {

                    Grade grade = new Grade(
                            Long.parseLong(record.get("id")),
                            Long.parseLong(record.get("studentId")),
                            Long.parseLong(record.get("subjectId")),
                            Double.parseDouble(record.get("grade")),
                            record.get("period")
                    );

                    grades.add(grade);
                }
            }

        } catch (IOException | NumberFormatException e) {
            throw new RuntimeException("Error importing grades from CSV", e);
        }

        return grades;
    }
}