# --- !Ups
ALTER TABLE contribution
ADD COLUMN document_simple tsvector;

UPDATE contribution
SET document_simple = to_tsvector('simple', unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

CREATE INDEX textsearchwords_idx ON contribution USING gin(document_simple);

CREATE OR REPLACE FUNCTION contribution_trigger() RETURNS trigger AS $$
begin
  new.document := to_tsvector(new.lang::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));
  new.document_simple := to_tsvector('simple', unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));
  return new;
end
$$ LANGUAGE plpgsql;
    
ALTER TABLE non_member_author ADD COLUMN publishContact BOOLEAN DEFAULT FALSE;
ALTER TABLE non_member_author ADD COLUMN subscribed BOOLEAN DEFAULT FALSE;
ALTER TABLE non_member_author ADD COLUMN Phone varchar(30) DEFAULT '';

# --- !Downs
ALTER TABLE non_member_author DROP COLUMN publishContact;
ALTER TABLE non_member_author DROP COLUMN subscribed;
ALTER TABLE non_member_author DROP COLUMN Phone;