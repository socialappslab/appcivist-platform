# --- !Ups
-- Extending field definitions to include also the type of the field value
alter table custom_field_definition add column field_type varchar(40) default 'TEXT';

-- Adding some default values that are useful
ALTER TABLE "public"."appcivist_user" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."assembly" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."ballot" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."ballot_paper" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."campaign" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."candidate" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component_definition" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."config" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."config_definition" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."contribution" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."custom_field_definition" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."custom_field_value" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."notification_event_signal" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."organization" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."resource" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."resource_space" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."s3file" ALTER COLUMN "id" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."token_action" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."user_profile" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."working_group" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();

# --- !Downs
