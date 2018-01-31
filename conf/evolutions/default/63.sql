# --- !Ups
ALTER TABLE "public"."subscription" DROP COLUMN default_identity;
ALTER TABLE "public"."subscription" ADD COLUMN default_identity text;

# --- !Downs
ALTER TABLE "public"."subscription" DROP COLUMN default_identity;
ALTER TABLE "public"."subscription" ADD COLUMN default_identity integer;
