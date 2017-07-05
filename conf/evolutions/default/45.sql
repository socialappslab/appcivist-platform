# --- !Ups
alter table contribution drop constraint ck_contrinution_contrinbutoin_status;

update contribution set status = 'DRAFT' where status = 'NEW';

ALTER TABLE contribution
  ADD CONSTRAINT ck_contrinution_contrinbutoin_status CHECK (status::text = ANY (ARRAY['NEW'::character varying, 'DRAFT'::character varying, 'PUBLISHED'::character varying,
  'ARCHIVED'::character varying, 'EXCLUDED'::character varying]::text[]));

# --- !Downs
