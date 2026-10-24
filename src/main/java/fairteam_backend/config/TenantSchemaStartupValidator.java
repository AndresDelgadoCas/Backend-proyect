package fairteam_backend.config;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/** Fails startup rather than serving account requests against a partial tenant schema. */
@Component
public class TenantSchemaStartupValidator implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(TenantSchemaStartupValidator.class);
    private static final List<String> REQUIRED_TABLES = List.of(
            "courses", "course_materials", "grades", "project_skill_requirements", "projects", "skills",
            "student_availability", "student_project_preferences", "student_skills",
            "students", "subjects", "team_students", "teams",
            "workshop_recommendations", "workshops", "assignments",
            "assignment_submissions", "grading_rubrics", "rubric_criteria", "ai_evaluations");

    private final JdbcTemplate jdbcTemplate;

    public TenantSchemaStartupValidator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        String requiredTables = REQUIRED_TABLES.stream()
                .map(table -> "('" + table + "')")
                .collect(Collectors.joining(", "));
        String sql = "SELECT expected_table.table_name FROM (VALUES " + requiredTables
                + ") AS expected_table(table_name) "
                + "WHERE NOT EXISTS (SELECT 1 FROM information_schema.columns column_info "
                + "WHERE column_info.table_schema = current_schema() "
                + "AND column_info.table_name = expected_table.table_name "
                + "AND column_info.column_name = 'teacher_id') ORDER BY expected_table.table_name";
        List<String> missingColumns = jdbcTemplate.queryForList(sql, String.class);
        if (!missingColumns.isEmpty()) {
            throw new IllegalStateException("Tenant database migration is incomplete; missing teacher_id on: "
                    + String.join(", ", missingColumns));
        }
        logger.info("Validated tenant schema for {} academic tables.", REQUIRED_TABLES.size());
    }
}
