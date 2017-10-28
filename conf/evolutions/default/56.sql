# --- !Ups
-- 56.sql
ALTER TABLE "public"."campaign" ADD COLUMN "brief" text;

# --- !Downs
ALTER TABLE "public"."campaign" DROP COLUMN "brief" text;

