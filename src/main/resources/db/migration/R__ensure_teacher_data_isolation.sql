-- Repeatable safety net: restore tenant columns and policies if version history is ahead of the schema.
-- recorded or skipped without creating tenant_bootstrap_lock and its policies.
-- The statements are idempotent so this can safely follow V2.
-- Give every row in each academic workspace an owner. Existing rows remain
-- hidden until the first account claims them during registration.
ALTER TABLE grades ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE project_skill_requirements ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE projects ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE skills ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE student_availability ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE student_project_preferences ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE student_skills ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE students ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE team_students ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE teams ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE workshop_recommendations ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE workshops ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE assignment_submissions ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE grading_rubrics ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE rubric_criteria ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE ai_evaluations ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);

ALTER TABLE students DROP CONSTRAINT IF EXISTS students_email_key;
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS subjects_name_key;
ALTER TABLE skills DROP CONSTRAINT IF EXISTS skills_name_key;
CREATE UNIQUE INDEX IF NOT EXISTS uq_students_teacher_email ON students(teacher_id, email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uq_subjects_teacher_name ON subjects(teacher_id, name);
CREATE UNIQUE INDEX IF NOT EXISTS uq_skills_teacher_name ON skills(teacher_id, name);

CREATE TABLE IF NOT EXISTS tenant_bootstrap_lock (id INTEGER PRIMARY KEY CHECK (id = 1));
INSERT INTO tenant_bootstrap_lock(id) VALUES (1) ON CONFLICT (id) DO NOTHING;

CREATE OR REPLACE FUNCTION fairteam_assign_tenant_before_insert() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.teacher_id IS NULL THEN
        NEW.teacher_id := NULLIF(current_setting('app.teacher_id', true), '')::BIGINT;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    table_name TEXT;
BEGIN
    FOREACH table_name IN ARRAY ARRAY[
        'courses',
        'grades', 'project_skill_requirements', 'projects', 'skills',
        'student_availability', 'student_project_preferences', 'student_skills',
        'students', 'subjects', 'team_students', 'teams',
        'workshop_recommendations', 'workshops', 'assignments',
        'assignment_submissions', 'grading_rubrics', 'rubric_criteria', 'ai_evaluations'
    ] LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS fairteam_assign_tenant ON %I', table_name);
        EXECUTE format('CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON %I FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert()', table_name);
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', table_name);
        EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', table_name);
        EXECUTE format('DROP POLICY IF EXISTS fairteam_tenant_isolation ON %I', table_name);
        EXECUTE format(
            'CREATE POLICY fairteam_tenant_isolation ON %I USING (teacher_id = NULLIF(current_setting(''app.teacher_id'', true), '''')::BIGINT OR (teacher_id IS NULL AND current_setting(''app.bootstrap'', true) = ''true'')) WITH CHECK (teacher_id = NULLIF(current_setting(''app.teacher_id'', true), '''')::BIGINT)',
            table_name
        );
    END LOOP;
END;
$$;

CREATE INDEX IF NOT EXISTS idx_grades_teacher_id ON grades(teacher_id);
CREATE INDEX IF NOT EXISTS idx_students_teacher_id ON students(teacher_id);
CREATE INDEX IF NOT EXISTS idx_subjects_teacher_id ON subjects(teacher_id);
CREATE INDEX IF NOT EXISTS idx_projects_teacher_id ON projects(teacher_id);
CREATE INDEX IF NOT EXISTS idx_teams_teacher_id ON teams(teacher_id);
CREATE INDEX IF NOT EXISTS idx_workshops_teacher_id ON workshops(teacher_id);
