# --- !Ups

CREATE EXTENSION pgagent;

DO $$
DECLARE
    jid integer;;
    scid integer;;
BEGIN
-- Creating a new job
INSERT INTO pgagent.pga_job(
    jobjclid, jobname, jobdesc, jobhostagent, jobenabled
) VALUES (
    1::integer, 'Notification Archival Job'::text, ''::text, ''::text, true
) RETURNING jobid INTO jid;;

-- Steps
-- Inserting a step (jobid: NULL)
INSERT INTO pgagent.pga_jobstep (
    jstjobid, jstname, jstenabled, jstkind,
    jstconnstr, jstdbname, jstonerror,
    jstcode, jstdesc
) VALUES (
    jid, 'Notification Arhival Job'::text, true, 's'::character(1),
    ''::text, 'appcivistcore'::name, 'f'::character(1),
    'select * from move_signals((select ''now''::timestamp - ''1 month''::interval));;'::text, ''::text
) ;;

-- Schedules
-- Inserting a schedule
INSERT INTO pgagent.pga_schedule(
    jscjobid, jscname, jscdesc, jscenabled,
    jscstart, jscend,    jscminutes, jschours, jscweekdays, jscmonthdays, jscmonths
) VALUES (
    jid, 'Notification Schedule'::text, ''::text, true,
    '2017-12-04 10:18:28-03'::timestamp with time zone, '2030-12-18 10:18:13-03'::timestamp with time zone,
    -- Minutes
    ARRAY[false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false]::boolean[],
    -- Hours
    ARRAY[true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false]::boolean[],
    -- Week days
    ARRAY[true, false, false, false, false, false, false]::boolean[],
    -- Month days
    ARRAY[false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false]::boolean[],
    -- Months
    ARRAY[false, false, false, false, false, false, false, false, false, false, false, false]::boolean[]
) RETURNING jscid INTO scid;;
END;;
$$

# --- !Downs
