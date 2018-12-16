# --- !Ups
CREATE EXTENSION unaccent;

CREATE TEXT SEARCH CONFIGURATION en ( COPY = english );
ALTER TEXT SEARCH CONFIGURATION en ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, english_stem;    

CREATE TEXT SEARCH CONFIGURATION es ( COPY = spanish );
ALTER TEXT SEARCH CONFIGURATION es ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, spanish_stem;

CREATE TEXT SEARCH CONFIGURATION it ( COPY = italian );
ALTER TEXT SEARCH CONFIGURATION it ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, italian_stem;

CREATE TEXT SEARCH CONFIGURATION fr ( COPY = french );
ALTER TEXT SEARCH CONFIGURATION fr ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, french_stem;


ALTER TABLE contribution
ADD COLUMN document tsvector;

UPDATE contribution 
SET document = to_tsvector(contribution.lang::regconfig, unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

CREATE INDEX textsearch_idx ON contribution USING gin(document);

CREATE OR REPLACE FUNCTION contribution_trigger() RETURNS trigger AS $$
begin
  new.document := to_tsvector(new.lang::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));;
  return new;;
end;;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE
    ON contribution FOR EACH ROW EXECUTE PROCEDURE contribution_trigger();
    
# --- !Downs
