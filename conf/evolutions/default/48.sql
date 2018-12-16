# --- !Ups

ALTER TABLE "public"."contribution" ALTER COLUMN "source" SET DEFAULT 'AppCivist Online';
UPDATE contribution SET source = 'AppCivist Oline';
# --- !Downs
