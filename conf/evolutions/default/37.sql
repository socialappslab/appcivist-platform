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
    
# --- !Downs