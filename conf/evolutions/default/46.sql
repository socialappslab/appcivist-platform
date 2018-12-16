# --- !Ups
CREATE OR REPLACE FUNCTION lang_contribution(param character varying, OUT result character varying) AS
$$
BEGIN

CASE
   WHEN param = 'es' THEN
      result := 'simple_spanish';;
   WHEN param = 'es-ES' THEN
      result := 'simple_spanish';;
   WHEN param = 'us' THEN
      result := 'simple_english';;
   WHEN param = 'en-US' THEN
      result := 'simple_english';;
   WHEN param = 'it' THEN
      result := 'simple_italian';;
   WHEN param = 'it-IT' THEN
      result := 'simple_italian';;
   WHEN param = 'fr' THEN
      result := 'simple_french';;
   WHEN param = 'fr-FR' THEN
      result := 'simple_french';;
   ELSE
      result := 'simple_english';;
END CASE;;

END;;
$$ LANGUAGE plpgsql;

CREATE TEXT SEARCH DICTIONARY simple_english
   (TEMPLATE = pg_catalog.simple, STOPWORDS = english);

CREATE TEXT SEARCH CONFIGURATION simple_english
   (copy = english);
ALTER TEXT SEARCH CONFIGURATION simple_english
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_english;

CREATE TEXT SEARCH DICTIONARY simple_spanish
   (TEMPLATE = pg_catalog.simple, STOPWORDS = spanish);

CREATE TEXT SEARCH CONFIGURATION simple_spanish
   (copy = spanish);
ALTER TEXT SEARCH CONFIGURATION simple_spanish
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_spanish;

CREATE TEXT SEARCH DICTIONARY simple_french
   (TEMPLATE = pg_catalog.simple, STOPWORDS = french);

CREATE TEXT SEARCH CONFIGURATION simple_french
   (copy = french);
ALTER TEXT SEARCH CONFIGURATION simple_french
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_french;

CREATE TEXT SEARCH DICTIONARY simple_italian
   (TEMPLATE = pg_catalog.simple, STOPWORDS = italian);

CREATE TEXT SEARCH CONFIGURATION simple_italian
   (copy = italian);
ALTER TEXT SEARCH CONFIGURATION simple_italian
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_italian;

CREATE OR REPLACE FUNCTION contribution_trigger() RETURNS trigger AS $$
begin
  new.document := to_tsvector(new.lang::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));;
  new.document_simple := to_tsvector(lang_contribution(new.lang)::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));;
  return new;;
end;;
$$ LANGUAGE plpgsql;

UPDATE contribution
SET document_simple = to_tsvector(lang_contribution(lang)::regconfig, unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));
# --- !Downs
