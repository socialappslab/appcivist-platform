# --- !Ups

--ALTER TABLE contribution DROP CONSTRAINT ck_contribution_contribution_status;
ALTER TABLE contribution ADD CONSTRAINT ck_contribution_contribution_status CHECK (status::text = ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text, 'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text, 'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text]));

# --- !Downs
