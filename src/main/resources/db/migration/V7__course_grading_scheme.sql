-- Grading scheme per course (number of periods and their weights) used by the grade simulator.
-- Same tenant isolation as every academic table: teacher_id is filled by trigger and enforced by RLS.
CREATE TABLE IF NOT EXISTS course_grading_schemes (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    weights VARCHAR(60) NOT NULL,
    passing_grade NUMERIC(3, 1) NOT NULL DEFAULT 3.0 CHECK (passing_grade > 0 AND passing_grade <= 5),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_course_grading_scheme UNIQUE (course_id)
);

CREATE INDEX IF NOT EXISTS idx_course_grading_schemes_teacher ON course_grading_schemes(teacher_id);

DROP TRIGGER IF EXISTS fairteam_assign_tenant ON course_grading_schemes;
CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON course_grading_schemes
FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert();

ALTER TABLE course_grading_schemes ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_grading_schemes FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS fairteam_tenant_isolation ON course_grading_schemes;
CREATE POLICY fairteam_tenant_isolation ON course_grading_schemes
USING (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT)
WITH CHECK (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT);
