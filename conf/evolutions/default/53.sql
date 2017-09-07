# --- !Ups
-- 53.sql
ALTER TABLE "public"."campaign" ADD COLUMN "external_ballot" text;
ALTER TABLE "public"."campaign" RENAME COLUMN "binding_ballot" TO "current_ballot";
ALTER TABLE "public"."campaign" DROP COLUMN "consultive_ballot";


# --- !Downs
ALTER TABLE "public"."campaign" DROP COLUMN "external_ballot";
ALTER TABLE "public"."campaign" RENAME COLUMN "current_ballot" TO "binding_ballot";
ALTER TABLE "public"."campaign" ADD COLUMN "consultive_ballot" character varying(40);
