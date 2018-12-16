# --- !Ups

ALTER TABLE public.config DROP CONSTRAINT ck_config_config_target;
ALTER TABLE public.config ADD CONSTRAINT ck_config_config_target CHECK (config_target::text = ANY (ARRAY['ASSEMBLY'::character varying::text, 'CAMPAIGN'::character varying::text, 'COMPONENT'::character varying::text, 'WORKING_GROUP'::character varying::text, 'MODULE'::character varying::text, 'PROPOSAL'::character varying::text, 'CONTRIBUTION'::character varying::text,'USER'::character varying::text]))

# --- !Downs
