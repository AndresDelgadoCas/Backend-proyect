-- Attendance per class session and a log of the support each student received.
-- Same tenant isolation as every academic table: teacher_id is filled by trigger and enforced by RLS.
CREATE TABLE IF NOT EXISTS attendance_records (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    session_date DATE NOT NULL,
    status VARCHAR(10) NOT NULL CHECK (status IN ('PRESENT', 'ABSENT', 'LATE', 'EXCUSED')),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attendance_session UNIQUE (course_id, student_id, session_date)
);
CREATE INDEX IF NOT EXISTS idx_attendance_course_date ON attendance_records(course_id, session_date);
CREATE INDEX IF NOT EXISTS idx_attendance_teacher ON attendance_records(teacher_id);

CREATE TABLE IF NOT EXISTS student_interventions (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('CONVERSATION', 'TUTORING', 'WORKSHOP', 'MESSAGE', 'FAMILY', 'OTHER')),
    note VARCHAR(500) NOT NULL DEFAULT '',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_interventions_student ON student_interventions(course_id, student_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_interventions_teacher ON student_interventions(teacher_id);

DO $$
DECLARE
    table_name TEXT;
BEGIN
    FOREACH table_name IN ARRAY ARRAY['attendance_records', 'student_interventions'] LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS fairteam_assign_tenant ON %I', table_name);
        EXECUTE format('CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON %I FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert()', table_name);
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', table_name);
        EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', table_name);
        EXECUTE format('DROP POLICY IF EXISTS fairteam_tenant_isolation ON %I', table_name);
        EXECUTE format('CREATE POLICY fairteam_tenant_isolation ON %I '
            'USING (teacher_id = NULLIF(current_setting(''app.teacher_id'', true), '''')::BIGINT) '
            'WITH CHECK (teacher_id = NULLIF(current_setting(''app.teacher_id'', true), '''')::BIGINT)', table_name);
    END LOOP;
END $$;
