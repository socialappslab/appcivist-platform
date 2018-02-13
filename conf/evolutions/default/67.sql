CREATE OR REPLACE FUNCTION create_contribution_audits() RETURNS void AS
$BODY$
DECLARE
    r contribution%rowtype;
BEGIN
    FOR r IN SELECT * FROM contribution
    LOOP
        INSERT INTO public.contribution_status_audit(
            status_start_date, status_end_date, contribution_contribution_id,
            status)
            VALUES (now(), null, r.contribution_id,
                    r.status);
    END LOOP;
    RETURN;
END
$BODY$
LANGUAGE plpgsql;

SELECT * FROM create_contribution_audits();

DROP FUNCTION create_contribution_audits();