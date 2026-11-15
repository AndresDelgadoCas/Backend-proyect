-- Super administrator support.
-- 1. Last sign-in per account and an append-only audit log of administrator actions.
-- 2. A read-only policy on every tenant table so an administrator request (app.admin = 'true',
--    set by the backend only after it re-checks the role in teacher_users) can count and list
--    data across accounts. Writes still go through the per-teacher policy, so an administrator
--    can never modify a teacher's academic data.
ALTER TABLE teacher_users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

CREATE TABLE IF NOT EXISTS admin_audit_log (
    id BIGSERIAL PRIMARY KEY,
    admin_id BIGINT NOT NULL REFERENCES teacher_users(id),
    action VARCHAR(60) NOT NULL,
    target_id BIGINT,
    detail VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_admin_audit_log_created ON admin_audit_log(created_at DESC);

DO $$
DECLARE
    tenant_table TEXT;
BEGIN
    FOR tenant_table IN
        SELECT cl.relname
        FROM pg_class cl
        JOIN information_schema.columns col
          ON col.table_name = cl.relname AND col.table_schema = current_schema()
        WHERE cl.relnamespace = current_schema()::regnamespace
          AND cl.relkind = 'r'
          AND cl.relrowsecurity
          AND col.column_name = 'teacher_id'
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS fairteam_admin_read ON %I', tenant_table);
        EXECUTE format(
            'CREATE POLICY fairteam_admin_read ON %I FOR SELECT USING (current_setting(''app.admin'', true) = ''true'')',
            tenant_table);
    END LOOP;
END;
$$;
