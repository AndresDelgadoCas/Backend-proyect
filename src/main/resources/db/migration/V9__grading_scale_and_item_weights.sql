-- Grading scales other than 0-5 (10, 20, 100...) and a weight for every graded activity.
--
-- Grades stay stored on the 0-5 scale the analysis uses: a grade is saved as (value / max_grade) * 5.
-- Four decimals keep values such as 87.5/100 = 4.375 exact; displaying them in the course's scale is
-- a multiplication done by the client.
ALTER TABLE grades ALTER COLUMN grade TYPE NUMERIC(6, 4);

-- The scale belongs to the course (not to its corte scheme, whose row also fixes the number of cortes).
ALTER TABLE courses ADD COLUMN IF NOT EXISTS max_grade NUMERIC(7, 2) NOT NULL DEFAULT 5;
ALTER TABLE courses DROP CONSTRAINT IF EXISTS chk_course_max_grade;
ALTER TABLE courses ADD CONSTRAINT chk_course_max_grade CHECK (max_grade > 0 AND max_grade <= 1000);

-- Percentage of each activity (subject column) inside one corte of one course.
CREATE TABLE IF NOT EXISTS grade_item_weights (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    period VARCHAR(20) NOT NULL,
    subject_id BIGINT NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    weight NUMERIC(5, 2) CHECK (weight IS NULL OR (weight >= 0 AND weight <= 100)),
    position INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_item_weight UNIQUE (course_id, period, subject_id)
);
CREATE INDEX IF NOT EXISTS idx_item_weights_course ON grade_item_weights(course_id, period);
CREATE INDEX IF NOT EXISTS idx_item_weights_teacher ON grade_item_weights(teacher_id);

DROP TRIGGER IF EXISTS fairteam_assign_tenant ON grade_item_weights;
CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON grade_item_weights
    FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert();
ALTER TABLE grade_item_weights ENABLE ROW LEVEL SECURITY;
ALTER TABLE grade_item_weights FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS fairteam_tenant_isolation ON grade_item_weights;
CREATE POLICY fairteam_tenant_isolation ON grade_item_weights
    USING (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT)
    WITH CHECK (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT);
