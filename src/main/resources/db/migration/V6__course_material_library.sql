CREATE TABLE IF NOT EXISTS course_materials (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(150) NOT NULL,
    file_size BIGINT NOT NULL CHECK (file_size > 0 AND file_size <= 10485760),
    content BYTEA NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_course_materials_tenant_course
    ON course_materials(teacher_id, course_id, created_at DESC);

CREATE OR REPLACE FUNCTION fairteam_assign_tenant_before_insert() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.teacher_id IS NULL THEN
        NEW.teacher_id := NULLIF(current_setting('app.teacher_id', true), '')::BIGINT;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS fairteam_assign_tenant ON course_materials;
CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON course_materials
FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert();

ALTER TABLE course_materials ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_materials FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS fairteam_tenant_isolation ON course_materials;
CREATE POLICY fairteam_tenant_isolation ON course_materials
USING (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT)
WITH CHECK (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT);
