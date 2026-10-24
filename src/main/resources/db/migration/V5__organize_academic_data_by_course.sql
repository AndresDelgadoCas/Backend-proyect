CREATE TABLE IF NOT EXISTS courses (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    name VARCHAR(120) NOT NULL,
    period VARCHAR(40),
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_courses_teacher_name ON courses(teacher_id, lower(name));
CREATE INDEX IF NOT EXISTS idx_courses_teacher ON courses(teacher_id);

-- Versioned migrations run before repeatable tenant-isolation scripts on a fresh database.
ALTER TABLE students ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE grades ADD COLUMN IF NOT EXISTS teacher_id BIGINT REFERENCES teacher_users(id);
ALTER TABLE students ADD COLUMN IF NOT EXISTS course_id BIGINT REFERENCES courses(id);
ALTER TABLE grades ADD COLUMN IF NOT EXISTS course_id BIGINT REFERENCES courses(id);

-- Run the one-time backfill as the schema owner, even when the old tenant policy is forced.
ALTER TABLE students NO FORCE ROW LEVEL SECURITY;
ALTER TABLE grades NO FORCE ROW LEVEL SECURITY;

ALTER TABLE grades DROP CONSTRAINT IF EXISTS uq_student_subject_period;
CREATE UNIQUE INDEX IF NOT EXISTS uq_course_student_subject_period
    ON grades(course_id, student_id, subject_id, period) WHERE course_id IS NOT NULL;

-- Preserve existing teacher-owned rosters and grades in an initial course.
INSERT INTO courses(teacher_id, name, description)
SELECT DISTINCT student.teacher_id, 'Curso importado', 'Datos existentes antes de organizar por cursos.'
FROM students student
WHERE student.teacher_id IS NOT NULL
  AND NOT EXISTS (
      SELECT 1 FROM courses course
      WHERE course.teacher_id = student.teacher_id
        AND lower(course.name) = lower('Curso importado')
  );

UPDATE students student
SET course_id = course.id
FROM courses course
WHERE course.teacher_id = student.teacher_id
  AND lower(course.name) = lower('Curso importado')
  AND student.course_id IS NULL;

UPDATE grades grade
SET course_id = student.course_id
FROM students student
WHERE grade.student_id = student.id
  AND grade.course_id IS NULL
  AND student.course_id IS NOT NULL;

ALTER TABLE students FORCE ROW LEVEL SECURITY;
ALTER TABLE grades FORCE ROW LEVEL SECURITY;

CREATE OR REPLACE FUNCTION fairteam_assign_tenant_before_insert() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.teacher_id IS NULL THEN
        NEW.teacher_id := NULLIF(current_setting('app.teacher_id', true), '')::BIGINT;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS fairteam_assign_tenant ON courses;
CREATE TRIGGER fairteam_assign_tenant BEFORE INSERT ON courses
FOR EACH ROW EXECUTE FUNCTION fairteam_assign_tenant_before_insert();

ALTER TABLE courses ENABLE ROW LEVEL SECURITY;
ALTER TABLE courses FORCE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS fairteam_tenant_isolation ON courses;
CREATE POLICY fairteam_tenant_isolation ON courses
USING (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT)
WITH CHECK (teacher_id = NULLIF(current_setting('app.teacher_id', true), '')::BIGINT);
