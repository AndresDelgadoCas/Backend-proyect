-- Workshops, quizzes and custom activities created for a course. They used to live only in the browser;
-- storing them lets rubrics, grade columns and Búho find them from any device.
CREATE TABLE IF NOT EXISTS course_activities (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    kind VARCHAR(20) NOT NULL CHECK (kind IN ('workshop', 'quiz', 'custom')),
    title VARCHAR(200) NOT NULL,
    topic VARCHAR(150),
    subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    target_level VARCHAR(10),
    content JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_course_activities_course ON course_activities(course_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_course_activities_subject ON course_activities(subject_id);
CREATE INDEX IF NOT EXISTS idx_course_activities_teacher ON course_activities(teacher_id);

DROP TRIGGER IF EXISTS fairteam_assign_tenant ON course_activities;
CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON course_activities
    FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert();
ALTER TABLE course_activities ENABLE ROW LEVEL SECURITY;
ALTER TABLE course_activities FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS fairteam_tenant_isolation ON course_activities;
CREATE POLICY fairteam_tenant_isolation ON course_activities
    USING (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT)
    WITH CHECK (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT);

-- A rubric can evaluate one activity; the topic links it to the grade book column of that subject.
ALTER TABLE course_rubrics ADD COLUMN IF NOT EXISTS activity_id BIGINT REFERENCES course_activities(id) ON DELETE SET NULL;
CREATE INDEX IF NOT EXISTS idx_course_rubrics_activity ON course_rubrics(activity_id);
