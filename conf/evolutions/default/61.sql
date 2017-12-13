# --- !Ups
ALTER TABLE "public"."resource" ADD COLUMN "resource_auth_key" text;
ALTER TABLE "public"."contribution" ADD COLUMN "extended_text_pad_resource_number" integer DEFAULT 1;
ALTER TABLE "public"."resource" ADD COLUMN "is_template" boolean DEFAULT false;


# --- !Downs
ALTER TABLE "public"."resource" DROP COLUMN "resource_auth_key" text;
ALTER TABLE "public"."contribution" DROP COLUMN "extended_text_pad_resource_number" integer;
ALTER TABLE "public"."resource" DROP COLUMN "is_template" boolean;

