# --- !Ups
-- 53.sql
ALTER TABLE campaign ADD COLUMN external_ballot text;
ALTER TABLE campaign RENAME COLUMN binding_ballot TO current_ballot;
ALTER TABLE campaign DROP COLUMN consultive_ballot;


# --- !Downs
ALTER TABLE campaign DROP COLUMN external_ballot;
ALTER TABLE campaign RENAME COLUMN current_ballot TO binding_ballot;
ALTER TABLE campaign ADD COLUMN consultive_ballot character varying(40);
