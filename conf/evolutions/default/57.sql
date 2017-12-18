# --- !Ups
CREATE TABLE notification_event_signal_archive
(
  id bigint NOT NULL,
  creation timestamp without time zone,
  last_update timestamp without time zone,
  lang character varying(255),
  removal timestamp without time zone,
  removed boolean,
  space_type character varying(255),
  signal_type character varying(255),
  event_id character varying(40),
  text text,
  title character varying(255),
  data jsonb,
  CONSTRAINT pk_notification_event_archive PRIMARY KEY (id)
)
WITH (
OIDS=FALSE
);
ALTER TABLE notification_event_signal_archive
  OWNER TO postgres;

ALTER TABLE resource
  DROP CONSTRAINT ck_resource_resource_type;

ALTER TABLE resource
  ADD CONSTRAINT ck_resource_resource_type CHECK (resource_type::text = ANY (ARRAY['PICTURE'::character varying::text, 'VIDEO'::character varying::text, 'PAD'::character varying::text, 'TEXT'::character varying::text, 'WEBPAGE'::character varying::text, 'FILE'::character varying::text, 'AUDIO'::character varying::text, 'CONTRIBUTION_TEMPLATE'::character varying::text, 'CAMPAIGN_TEMPLATE'::character varying::text, 'PROPOSAL'::character varying::text, 'GDOC'::character varying::text]))

ALTER TABLE "public"."appcivist_user"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."assembly"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."assembly_profile"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."ballot"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."ballot_configuration"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."ballot_paper"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."campaign"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."campaign_required_configuration"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."campaign_template"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."campaign_timeline_edge"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."candidate"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."component"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_definition"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_milestone"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_required_configuration"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_required_milestone"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."config"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."config_definition"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_feedback"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_history"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_publish_history"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_statistics"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_template"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_template_section"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."custom_field_definition"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."custom_field_value"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."custom_field_value_option"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."geo"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."hashtag"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."membership"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."membership_invitation"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."non_member_author"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."notification_event_signal"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."notification_event_signal_archive"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."notification_event_signal_user"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."organization"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."resource"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."resource_space"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."theme"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."token_action" ALTER COLUMN "created" SET DEFAULT now();
ALTER TABLE "public"."user_profile"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."vote"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."working_group"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."working_group_profile"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();

# --- !Downs

