# How To Install and Configure Pgagent on Ubuntu 16.04

This guide is based in the official Pgagent documentation available in: 
https://www.pgadmin.org/docs/pgadmin4/1.x/pgagent.html

pgAgent is a job scheduling agent for Postgres databases, capable of running multi-step batch or shell scripts and SQL tasks on complex schedules.

## Requirements
- postgresql
- pgAdmin 4

## Installation

Install pgAgent

```
sudo apt-get install pgagent
```

Create pgAgent extension

```
sudo -i -u postgres
psql
CREATE EXTENSION pgagent;
CREATE LANGUAGE plpgsql;
```

Deamon installation, run:

```
/path/to/pgagent hostaddr=127.0.0.1 dbname=postgres user=postgres
```
After the execution of the sqls to create the archival tables and function, we can create the
job and schedule to execute the function created.
For create the notification job execute the next SQL code, replacing the string $DATE$ to the date-time
from where remove the notification signals and $START_DATE$ and $END_DATE$ to change the start and
end date for the schedule. Also, in the week, month, etc array each boolean variable represents a
days of the week (starting with Sunday), day of month, hour, etc. Currently the scheduler is configured
for repeat his job every Sunday at 00:00:
``` postgresplsql
DO $$
DECLARE
    jid integer;
    scid integer;
BEGIN
-- Creating a new job
INSERT INTO pgagent.pga_job(
    jobjclid, jobname, jobdesc, jobhostagent, jobenabled
) VALUES (
    1::integer, 'Notification Archival Job'::text, ''::text, ''::text, true
) RETURNING jobid INTO jid;

-- Steps
-- Inserting a step (jobid: NULL)
INSERT INTO pgagent.pga_jobstep (
    jstjobid, jstname, jstenabled, jstkind,
    jstconnstr, jstdbname, jstonerror,
    jstcode, jstdesc
) VALUES (
    jid, 'Notification Arhival Job'::text, true, 's'::character(1),
    ''::text, 'appcivistcore'::name, 'f'::character(1),
    'select * from move_signals(''2017-12-04'');'::text, ''::text
) ;

-- Schedules
-- Inserting a schedule
INSERT INTO pgagent.pga_schedule(
    jscjobid, jscname, jscdesc, jscenabled,
    jscstart, jscend,    jscminutes, jschours, jscweekdays, jscmonthdays, jscmonths
) VALUES (
    jid, 'Notification Schedule'::text, ''::text, true,
    '2017-12-04 10:18:28-03'::timestamp with time zone, '2017-12-18 10:18:13-03'::timestamp with time zone,
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
) RETURNING jscid INTO scid;
END
$$;
```

After this the job and schedule is configured and ready to run.
This configuration can edit and create also through the graphic interface of pgadmin4