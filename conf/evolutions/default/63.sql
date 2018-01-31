# --- !Downs

ALTER TABLE "public"."subscription" DROP COLUMN default_identity;
# --- !Ups

ALTER TABLE "public"."subscription" ADD COLUMN default_identity text;

