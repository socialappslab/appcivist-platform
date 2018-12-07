ALTER TABLE contribution DROP CONSTRAINT ck_contribution_contribution_status;

UPDATE contribution set status = 'MERGED' where STATUS ilike 'MERGE%';

ALTER TABLE contribution ADD CONSTRAINT ck_contribution_contribution_status CHECK (status::text =
ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text,
'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text,
'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text,
'FORKED_PRIVATE_DRAFT'::character varying::text, 'FORKED_PUBLIC_DRAFT'::character varying::text,
'FORKED_PUBLISHED'::character varying::text, 'MERGED'::character varying::text]]));


ALTER TABLE contribution_status_audit DROP CONSTRAINT ck_contribution_status_audit_status;

UPDATE contribution_status_audit set status = 'MERGED' where STATUS ilike 'MERGE%';

ALTER TABLE contribution_status_audit ADD CONSTRAINT ck_contribution_status_audit_status CHECK (status::text =
ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text,
'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text,
'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text,
'FORKED_PRIVATE_DRAFT'::character varying::text, 'FORKED_PUBLIC_DRAFT'::character varying::text,
'FORKED_PUBLISHED'::character varying::text, 'MERGED'::character varying::text]));