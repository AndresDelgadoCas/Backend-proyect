package fairteam_backend.adapter;

import java.util.List;
import fairteam_backend.entity.Grade;

public interface GradeAdapter {

    List<Grade> importGrades(String filePath);
}