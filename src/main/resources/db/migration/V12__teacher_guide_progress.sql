-- Progress of the "Primeros pasos" guide, one row per teacher, so it follows the account on any device.
CREATE TABLE IF NOT EXISTS teacher_guide_progress (
    teacher_id BIGINT PRIMARY KEY REFERENCES teacher_users(id) ON DELETE CASCADE,
    state JSONB NOT NULL,
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

DROP TRIGGER IF EXISTS fairteam_assign_tenant ON teacher_guide_progress;
CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON teacher_guide_progress
    FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert();
ALTER TABLE teacher_guide_progress ENABLE ROW LEVEL SECURITY;
ALTER TABLE teacher_guide_progress FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS fairteam_tenant_isolation ON teacher_guide_progress;
CREATE POLICY fairteam_tenant_isolation ON teacher_guide_progress
    USING (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT)
    WITH CHECK (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT);
