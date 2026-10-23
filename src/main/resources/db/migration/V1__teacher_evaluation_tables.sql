CREATE TABLE IF NOT EXISTS teacher_users (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL DEFAULT 'TEACHER',
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assignments (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL REFERENCES subjects(id),
    teacher_id BIGINT NOT NULL REFERENCES teacher_users(id),
    title VARCHAR(150) NOT NULL,
    description TEXT,
    instructions TEXT,
    due_date TIMESTAMP,
    max_score NUMERIC(10, 2) NOT NULL DEFAULT 5.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS assignment_submissions (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL REFERENCES assignments(id),
    student_id BIGINT NOT NULL REFERENCES students(id),
    submission_text TEXT,
    file_url TEXT,
    submitted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(255) NOT NULL DEFAULT 'SUBMITTED'
);

CREATE TABLE IF NOT EXISTS grading_rubrics (
    id BIGSERIAL PRIMARY KEY,
    assignment_id BIGINT NOT NULL UNIQUE REFERENCES assignments(id),
    name VARCHAR(150) NOT NULL,
    description TEXT
);

CREATE TABLE IF NOT EXISTS rubric_criteria (
    id BIGSERIAL PRIMARY KEY,
    rubric_id BIGINT NOT NULL REFERENCES grading_rubrics(id),
    name VARCHAR(150) NOT NULL,
    description TEXT,
    max_score NUMERIC(10, 2) NOT NULL,
    weight NUMERIC(5, 2) NOT NULL DEFAULT 1.00
);

CREATE TABLE IF NOT EXISTS ai_evaluations (
    id BIGSERIAL PRIMARY KEY,
    submission_id BIGINT NOT NULL UNIQUE REFERENCES assignment_submissions(id),
    status VARCHAR(255) NOT NULL DEFAULT 'PENDING',
    suggested_grade NUMERIC(5, 2),
    feedback TEXT,
    criteria_results JSONB NOT NULL DEFAULT '{}'::jsonb,
    reviewed_by BIGINT,
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_assignments_teacher_id ON assignments(teacher_id);
CREATE INDEX IF NOT EXISTS idx_submissions_student_id ON assignment_submissions(student_id);
CREATE INDEX IF NOT EXISTS idx_submissions_assignment_id ON assignment_submissions(assignment_id);
CREATE INDEX IF NOT EXISTS idx_ai_evaluations_status ON ai_evaluations(status);
