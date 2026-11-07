-- Analytic rubrics per course: criteria with four described levels, the level each student reached
-- and, once published, the grade book column where the resulting grade lives.
CREATE TABLE IF NOT EXISTS course_rubrics (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    course_id BIGINT NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(150) NOT NULL,
    description TEXT,
    period VARCHAR(20),
    subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX IF NOT EXISTS idx_course_rubrics_course ON course_rubrics(course_id);
CREATE INDEX IF NOT EXISTS idx_course_rubrics_teacher ON course_rubrics(teacher_id);

CREATE TABLE IF NOT EXISTS course_rubric_criteria (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    rubric_id BIGINT NOT NULL REFERENCES course_rubrics(id) ON DELETE CASCADE,
    position INT NOT NULL DEFAULT 0,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    weight NUMERIC(5, 2) NOT NULL CHECK (weight >= 0 AND weight <= 100),
    level_excellent TEXT NOT NULL,
    level_good TEXT NOT NULL,
    level_basic TEXT NOT NULL,
    level_insufficient TEXT NOT NULL
);
CREATE INDEX IF NOT EXISTS idx_rubric_criteria_rubric ON course_rubric_criteria(rubric_id, position);
CREATE INDEX IF NOT EXISTS idx_rubric_criteria_teacher ON course_rubric_criteria(teacher_id);

-- level: 0 Excelente, 1 Bueno, 2 Básico, 3 Insuficiente.
CREATE TABLE IF NOT EXISTS course_rubric_scores (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    rubric_id BIGINT NOT NULL REFERENCES course_rubrics(id) ON DELETE CASCADE,
    criterion_id BIGINT NOT NULL REFERENCES course_rubric_criteria(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    level SMALLINT NOT NULL CHECK (level BETWEEN 0 AND 3),
    CONSTRAINT uq_rubric_score UNIQUE (criterion_id, student_id)
);
CREATE INDEX IF NOT EXISTS idx_rubric_scores_rubric ON course_rubric_scores(rubric_id, student_id);
CREATE INDEX IF NOT EXISTS idx_rubric_scores_teacher ON course_rubric_scores(teacher_id);

-- A short note to the student, kept with the rubric so it can go out with the grade.
CREATE TABLE IF NOT EXISTS course_rubric_comments (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    rubric_id BIGINT NOT NULL REFERENCES course_rubrics(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    comment VARCHAR(1000) NOT NULL,
    CONSTRAINT uq_rubric_comment UNIQUE (rubric_id, student_id)
);
CREATE INDEX IF NOT EXISTS idx_rubric_comments_teacher ON course_rubric_comments(teacher_id);

DO $$
DECLARE
    t TEXT;
BEGIN
    FOREACH t IN ARRAY ARRAY['course_rubrics', 'course_rubric_criteria', 'course_rubric_scores', 'course_rubric_comments'] LOOP
        EXECUTE format('DROP TRIGGER IF EXISTS fairteam_assign_tenant ON %I', t);
        EXECUTE format('CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON %I '
            'FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert()', t);
        EXECUTE format('ALTER TABLE %I ENABLE ROW LEVEL SECURITY', t);
        EXECUTE format('ALTER TABLE %I FORCE ROW LEVEL SECURITY', t);
        EXECUTE format('DROP POLICY IF EXISTS fairteam_tenant_isolation ON %I', t);
        EXECUTE format('CREATE POLICY fairteam_tenant_isolation ON %I '
            'USING (teacher_id = NULLIF(current_setting(''app.teacher_id'', true), '''')::BIGINT) '
            'WITH CHECK (teacher_id = NULLIF(current_setting(''app.teacher_id'', true), '''')::BIGINT)', t);
    END LOOP;
END $$;
