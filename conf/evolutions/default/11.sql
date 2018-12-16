# --- !Ups

CREATE OR REPLACE FUNCTION setval_max
(
    schema_name name,
    table_name name DEFAULT NULL::name,
    raise_notice boolean DEFAULT false
)
RETURNS void AS
$BODY$

DECLARE
    row_data RECORD;;
    sql_code TEXT;;

BEGIN
    IF ((SELECT COUNT(*) FROM pg_namespace WHERE nspname = schema_name) = 0) THEN
        RAISE EXCEPTION 'The schema "%" does not exist', schema_name;;
    END IF;;

    FOR sql_code IN
        SELECT 'SELECT SETVAL(' ||quote_literal(N.nspname || '.' || S.relname)|| ', MAX(' ||quote_ident(C.attname)|| ') ) FROM ' || quote_ident(N.nspname) || '.' || quote_ident(T.relname) AS sql_code
            FROM pg_class AS S
            INNER JOIN pg_depend AS D ON S.oid = D.objid
            INNER JOIN pg_class AS T ON D.refobjid = T.oid
            INNER JOIN pg_attribute AS C ON D.refobjid = C.attrelid AND D.refobjsubid = C.attnum
            INNER JOIN pg_namespace N ON N.oid = S.relnamespace
            WHERE S.relkind = 'S' AND N.nspname = schema_name AND (table_name IS NULL OR T.relname = table_name)
            ORDER BY S.relname
    LOOP
        IF (raise_notice) THEN
            RAISE NOTICE 'sql_code: %', sql_code;;
        END IF;;
        EXECUTE sql_code;;
    END LOOP;;
END;;
$BODY$ LANGUAGE plpgsql VOLATILE;

# --- !Downs
drop function if exists setval_max();
