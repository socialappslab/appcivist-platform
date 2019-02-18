# --- !Ups

ALTER TABLE ballot
  DROP CONSTRAINT ck_ballot_status;

ALTER TABLE ballot
  ADD CONSTRAINT ck_ballot_status
  CHECK (status = ANY (ARRAY[0, 2]));

# --- !Downs
