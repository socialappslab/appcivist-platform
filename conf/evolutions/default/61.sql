# --- !Ups
ALTER TABLE "public"."resource" ADD COLUMN "resource_auth_key" text;
ALTER TABLE "public"."contribution" ADD COLUMN "extended_text_pad_resource_number" integer DEFAULT 1;
ALTER TABLE "public"."resource" ADD COLUMN "is_template" boolean DEFAULT false;
ALTER TABLE "public"."s3file" ADD COLUMN "creation" timestamp DEFAULT now();
ALTER TABLE "public"."subscription" ADD COLUMN "creation" timestamp DEFAULT now();
ALTER TABLE "public"."resource_space_association_history" ALTER COLUMN "creation" SET DEFAULT now();

# --- !Downs
ALTER TABLE "public"."resource" DROP COLUMN "resource_auth_key" text;
ALTER TABLE "public"."contribution" DROP COLUMN "extended_text_pad_resource_number" integer;
ALTER TABLE "public"."resource" DROP COLUMN "is_template" boolean;
ALTER TABLE "public"."s3file" DROP COLUMN "creation" timestamp;
ALTER TABLE "public"."subscription" DROP COLUMN "creation" timestamp;

