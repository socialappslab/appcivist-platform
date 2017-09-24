# --- !Ups
-- 54.sql
ALTER TABLE ballot ADD COLUMN votes_limit CHARACTER VARYING(40);
ALTER TABLE ballot ADD COLUMN votes_limit_meaning CHARACTER VARYING(15);
ALTER TABLE ballot
  ADD CONSTRAINT ck_ballot_votes_limit_meaning 
  CHECK (votes_limit_meaning::text = 
    ANY (ARRAY['SELECTIONS'::character varying, 'TOKENS'::character varying, 'RANGE'::character varying]));
   
ALTER TABLE contribution DROP CONSTRAINT ck_contrinution_contrinbutoin_status ;

ALTER TABLE contribution
  ADD CONSTRAINT ck_contrinution_contrinbutoin_status 
  CHECK (status::text = 
    ANY (ARRAY['NEW'::character varying, 'DRAFT'::character varying, 'PUBLISHED'::character varying,
                    'ARCHIVED'::character varying, 'EXCLUDED'::character varying, 'MODERATED'::character varying, 
                    'INBALLOT'::character varying, 'SELECTED'::character varying]::text[]));
    
# --- !Downs
ALTER TABLE ballot DROP COLUMN votes_limit;
ALTER TABLE ballot DROP COLUMN votes_limit_meaning;
ALTER TABLE ballot DROP CONSTRAINT ck_ballot_votes_limit_meaning;
