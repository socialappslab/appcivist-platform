# --- !Ups
ALTER TABLE resource DROP CONSTRAINT ck_resource_resource_type;
ALTER TABLE resource ADD CONSTRAINT ck_resource_resource_type CHECK (resource_type::text = ANY (ARRAY['PICTURE'::character varying::text, 'VIDEO'::character varying::text, 'PAD'::character varying::text, 'TEXT'::character varying::text, 'WEBPAGE'::character varying::text, 'FILE'::character varying::text, 'AUDIO'::character varying::text, 'CONTRIBUTION_TEMPLATE'::character varying::text, 'CAMPAIGN_TEMPLATE'::character varying::text, 'PROPOSAL'::character varying::text, 'GDOC'::character varying::text, 'PEERDOC'::character varying::text]));
# --- !Downs
