-- Flyway callback: runs before every migration.
--
-- V1 and later assume the original FairTeam academic tables already exist, which was true for the
-- first Render database but not for a brand-new one. On an empty database this creates those base
-- tables exactly as in the original schema (FairTeam-Database/schema.sql); on any existing database
-- it does nothing, because `students` is already there.
DO $fairteam_base$
BEGIN
    IF to_regclass('public.students') IS NULL THEN
        CREATE TABLE public.grades (
            id bigint NOT NULL,
            student_id bigint NOT NULL,
            subject_id bigint NOT NULL,
            grade numeric(3,2) NOT NULL,
            period character varying(20) NOT NULL,
            CONSTRAINT chk_grade_value CHECK (((grade >= (0)::numeric) AND (grade <= (5)::numeric)))
        );

        CREATE SEQUENCE public.grades_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.grades_id_seq OWNED BY public.grades.id;

        CREATE TABLE public.project_skill_requirements (
            project_id bigint NOT NULL,
            skill_id bigint NOT NULL,
            min_level integer NOT NULL,
            CONSTRAINT chk_required_skill_level CHECK (((min_level >= 1) AND (min_level <= 5)))
        );

        CREATE TABLE public.projects (
            id bigint NOT NULL,
            name character varying(150) NOT NULL,
            description text,
            team_size integer NOT NULL,
            created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
            CONSTRAINT chk_team_size CHECK ((team_size > 0))
        );

        CREATE SEQUENCE public.projects_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.projects_id_seq OWNED BY public.projects.id;

        CREATE TABLE public.skills (
            id bigint NOT NULL,
            name character varying(100) NOT NULL
        );

        CREATE SEQUENCE public.skills_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.skills_id_seq OWNED BY public.skills.id;

        CREATE TABLE public.student_availability (
            id bigint NOT NULL,
            student_id bigint NOT NULL,
            day_of_week integer NOT NULL,
            start_time time without time zone NOT NULL,
            end_time time without time zone NOT NULL,
            CONSTRAINT chk_availability_time CHECK ((start_time < end_time)),
            CONSTRAINT chk_day_of_week CHECK (((day_of_week >= 1) AND (day_of_week <= 7)))
        );

        CREATE SEQUENCE public.student_availability_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.student_availability_id_seq OWNED BY public.student_availability.id;

        CREATE TABLE public.student_project_preferences (
            id bigint NOT NULL,
            project_id bigint NOT NULL,
            student_id bigint NOT NULL,
            preferred_student_id bigint NOT NULL,
            preference_type character varying(10) NOT NULL,
            CONSTRAINT chk_different_students CHECK ((student_id <> preferred_student_id)),
            CONSTRAINT chk_preference_type CHECK (((preference_type)::text = ANY ((ARRAY['PREFER'::character varying, 'AVOID'::character varying])::text[])))
        );

        CREATE SEQUENCE public.student_project_preferences_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.student_project_preferences_id_seq OWNED BY public.student_project_preferences.id;

        CREATE TABLE public.student_skills (
            student_id bigint NOT NULL,
            skill_id bigint NOT NULL,
            level integer NOT NULL,
            CONSTRAINT chk_skill_level CHECK (((level >= 1) AND (level <= 5)))
        );

        CREATE TABLE public.students (
            id bigint NOT NULL,
            name character varying(120) NOT NULL,
            email character varying(180),
            academic_level character varying(10) DEFAULT 'MEDIUM'::character varying NOT NULL,
            created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
            CONSTRAINT chk_student_academic_level CHECK (((academic_level)::text = ANY ((ARRAY['HIGH'::character varying, 'MEDIUM'::character varying, 'LOW'::character varying])::text[])))
        );

        CREATE SEQUENCE public.students_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.students_id_seq OWNED BY public.students.id;

        CREATE TABLE public.subjects (
            id bigint NOT NULL,
            name character varying(120) NOT NULL
        );

        CREATE SEQUENCE public.subjects_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.subjects_id_seq OWNED BY public.subjects.id;

        CREATE TABLE public.team_students (
            team_id bigint NOT NULL,
            student_id bigint NOT NULL
        );

        CREATE TABLE public.teams (
            id bigint NOT NULL,
            project_id bigint NOT NULL,
            name character varying(100) NOT NULL,
            formation_strategy character varying(100),
            score numeric(5,2),
            explanation text,
            created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
            CONSTRAINT chk_team_score CHECK (((score IS NULL) OR ((score >= (0)::numeric) AND (score <= (100)::numeric))))
        );

        CREATE SEQUENCE public.teams_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.teams_id_seq OWNED BY public.teams.id;

        CREATE TABLE public.workshop_recommendations (
            id bigint NOT NULL,
            student_id bigint NOT NULL,
            workshop_id bigint NOT NULL,
            reason text NOT NULL,
            recommendation_score numeric(5,2),
            created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
            CONSTRAINT chk_recommendation_score CHECK (((recommendation_score IS NULL) OR ((recommendation_score >= (0)::numeric) AND (recommendation_score <= (100)::numeric))))
        );

        CREATE SEQUENCE public.workshop_recommendations_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.workshop_recommendations_id_seq OWNED BY public.workshop_recommendations.id;

        CREATE TABLE public.workshops (
            id bigint NOT NULL,
            name character varying(150) NOT NULL,
            description text,
            topic character varying(120) NOT NULL
        );

        CREATE SEQUENCE public.workshops_id_seq
            START WITH 1
            INCREMENT BY 1
            NO MINVALUE
            NO MAXVALUE
            CACHE 1;

        ALTER SEQUENCE public.workshops_id_seq OWNED BY public.workshops.id;

        ALTER TABLE ONLY public.grades ALTER COLUMN id SET DEFAULT nextval('public.grades_id_seq'::regclass);

        ALTER TABLE ONLY public.projects ALTER COLUMN id SET DEFAULT nextval('public.projects_id_seq'::regclass);

        ALTER TABLE ONLY public.skills ALTER COLUMN id SET DEFAULT nextval('public.skills_id_seq'::regclass);

        ALTER TABLE ONLY public.student_availability ALTER COLUMN id SET DEFAULT nextval('public.student_availability_id_seq'::regclass);

        ALTER TABLE ONLY public.student_project_preferences ALTER COLUMN id SET DEFAULT nextval('public.student_project_preferences_id_seq'::regclass);

        ALTER TABLE ONLY public.students ALTER COLUMN id SET DEFAULT nextval('public.students_id_seq'::regclass);

        ALTER TABLE ONLY public.subjects ALTER COLUMN id SET DEFAULT nextval('public.subjects_id_seq'::regclass);

        ALTER TABLE ONLY public.teams ALTER COLUMN id SET DEFAULT nextval('public.teams_id_seq'::regclass);

        ALTER TABLE ONLY public.workshop_recommendations ALTER COLUMN id SET DEFAULT nextval('public.workshop_recommendations_id_seq'::regclass);

        ALTER TABLE ONLY public.workshops ALTER COLUMN id SET DEFAULT nextval('public.workshops_id_seq'::regclass);

        ALTER TABLE ONLY public.grades
            ADD CONSTRAINT grades_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.project_skill_requirements
            ADD CONSTRAINT project_skill_requirements_pkey PRIMARY KEY (project_id, skill_id);

        ALTER TABLE ONLY public.projects
            ADD CONSTRAINT projects_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.skills
            ADD CONSTRAINT skills_name_key UNIQUE (name);

        ALTER TABLE ONLY public.skills
            ADD CONSTRAINT skills_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.student_availability
            ADD CONSTRAINT student_availability_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.student_project_preferences
            ADD CONSTRAINT student_project_preferences_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.student_skills
            ADD CONSTRAINT student_skills_pkey PRIMARY KEY (student_id, skill_id);

        ALTER TABLE ONLY public.students
            ADD CONSTRAINT students_email_key UNIQUE (email);

        ALTER TABLE ONLY public.students
            ADD CONSTRAINT students_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.subjects
            ADD CONSTRAINT subjects_name_key UNIQUE (name);

        ALTER TABLE ONLY public.subjects
            ADD CONSTRAINT subjects_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.team_students
            ADD CONSTRAINT team_students_pkey PRIMARY KEY (team_id, student_id);

        ALTER TABLE ONLY public.teams
            ADD CONSTRAINT teams_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.grades
            ADD CONSTRAINT uq_student_subject_period UNIQUE (student_id, subject_id, period);

        ALTER TABLE ONLY public.workshop_recommendations
            ADD CONSTRAINT workshop_recommendations_pkey PRIMARY KEY (id);

        ALTER TABLE ONLY public.workshops
            ADD CONSTRAINT workshops_pkey PRIMARY KEY (id);

        CREATE INDEX idx_availability_student ON public.student_availability USING btree (student_id);

        CREATE INDEX idx_grades_student ON public.grades USING btree (student_id);

        CREATE INDEX idx_grades_subject ON public.grades USING btree (subject_id);

        CREATE INDEX idx_project_requirements_skill ON public.project_skill_requirements USING btree (skill_id);

        CREATE INDEX idx_student_skills_skill ON public.student_skills USING btree (skill_id);

        CREATE INDEX idx_team_students_student ON public.team_students USING btree (student_id);

        CREATE INDEX idx_teams_project ON public.teams USING btree (project_id);

        CREATE INDEX idx_workshop_recommendations_student ON public.workshop_recommendations USING btree (student_id);

        ALTER TABLE ONLY public.student_availability
            ADD CONSTRAINT fk_availability_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.grades
            ADD CONSTRAINT fk_grade_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.grades
            ADD CONSTRAINT fk_grade_subject FOREIGN KEY (subject_id) REFERENCES public.subjects(id) ON DELETE RESTRICT;

        ALTER TABLE ONLY public.student_project_preferences
            ADD CONSTRAINT fk_preference_preferred_student FOREIGN KEY (preferred_student_id) REFERENCES public.students(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.student_project_preferences
            ADD CONSTRAINT fk_preference_project FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.student_project_preferences
            ADD CONSTRAINT fk_preference_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.project_skill_requirements
            ADD CONSTRAINT fk_project_requirement_project FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.project_skill_requirements
            ADD CONSTRAINT fk_project_requirement_skill FOREIGN KEY (skill_id) REFERENCES public.skills(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.workshop_recommendations
            ADD CONSTRAINT fk_recommendation_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.workshop_recommendations
            ADD CONSTRAINT fk_recommendation_workshop FOREIGN KEY (workshop_id) REFERENCES public.workshops(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.student_skills
            ADD CONSTRAINT fk_student_skill_skill FOREIGN KEY (skill_id) REFERENCES public.skills(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.student_skills
            ADD CONSTRAINT fk_student_skill_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.teams
            ADD CONSTRAINT fk_team_project FOREIGN KEY (project_id) REFERENCES public.projects(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.team_students
            ADD CONSTRAINT fk_team_student_student FOREIGN KEY (student_id) REFERENCES public.students(id) ON DELETE CASCADE;

        ALTER TABLE ONLY public.team_students
            ADD CONSTRAINT fk_team_student_team FOREIGN KEY (team_id) REFERENCES public.teams(id) ON DELETE CASCADE;
    END IF;
END
$fairteam_base$;
