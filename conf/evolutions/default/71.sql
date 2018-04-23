ALTER TABLE contribution
DROP CONSTRAINT ck_contrinution_contrinbutoin_status;
ALTER TABLE contribution
ADD CONSTRAINT ck_contribution_contribution_status CHECK (status::text = ANY (ARRAY['DRAFT'::character varying::text, 'PUBLISHED'::character varying::text, 'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text]))
