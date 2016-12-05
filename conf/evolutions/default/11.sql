# --- !Ups

-- Created by @josepmv
-- https://github.com/josepmv/dbadailystuff/blob/master/postgresql_setval_max.sql

-- SETVAL for all sequences in a schema or for a unique table
-- In PostgreSQL, when you’re working with sequences, if you insert a future value due to the incrementing values, you will get an error
--  when that value is going to be inserted. I like much more how SQL Server handles autoincrement columns with its IDENTITY property, 
--  that would be like the sequences linked to a table like SERIAL, but it’s much more restrictive and by default you cannot INSERT a register
--  specifying the value of this column as you can do with PostgreSQL.
-- The PostgreSQL setval() function, explained in Sequence Manipulation Functions (http://www.postgresql.org/docs/current/interactive/functions-sequence.html), 
-- is the way that PostgreSQL has to change the value of a sequence. But only accepts one table as a parameter. 
-- So, if you need to set all the sequences in a schema to the max(id) of every table, 
-- you can do can use the following script, based on Updating sequence values from table select (http://wiki.postgresql.org/wiki/Fixing_Sequences).
-- */

CREATE OR REPLACE FUNCTION setval_max
(
    schema_name name,
    table_name name DEFAULT NULL::name,
    raise_notice boolean DEFAULT false
)
RETURNS void AS
$BODY$

-- Sets all the sequences in the schema "schema_name" to the max(id) of every table (or a specific table, if name is supplied)
-- Examples:
--  SELECT setval_max('public');
--  SELECT setval_max('public','mytable');
--  SELECT setval_max('public',null,true);
--  SELECT setval_max('public','mytable',true);

DECLARE
    row_data RECORD;;
    sql_code TEXT;;

BEGIN
    IF ((SELECT COUNT(*) FROM pg_namespace WHERE nspname = schema_name) = 0) THEN
        RAISE EXCEPTION 'The schema "%" does not exist', schema_name;;
    END IF;;

    FOR sql_code IN
        SELECT 'SELECT SETVAL(' ||quote_literal(N.nspname || '.' || S.relname)|| ', MAX(' ||quote_ident(C.attname)|| ') ) FROM ' || quote_ident(N.nspname) || '.' || quote_ident(T.relname)|| ';' AS sql_code
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
$BODY$
LANGUAGE plpgsql VOLATILE;

# --- !Downs
drop function setval_max;