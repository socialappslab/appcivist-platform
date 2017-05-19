# --- !Ups
CREATE TEXT SEARCH CONFIGURATION "en-us" ( COPY = english );
ALTER TEXT SEARCH CONFIGURATION "en-us" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, english_stem;

CREATE TEXT SEARCH CONFIGURATION "es-es" ( COPY = spanish );
ALTER TEXT SEARCH CONFIGURATION "es-es" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, spanish_stem;

CREATE TEXT SEARCH CONFIGURATION "it-it" ( COPY = italian );
ALTER TEXT SEARCH CONFIGURATION "it-it" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, italian_stem;

CREATE TEXT SEARCH CONFIGURATION "fr-fr" ( COPY = french );
ALTER TEXT SEARCH CONFIGURATION "fr-fr" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, french_stem;

CREATE OR REPLACE FUNCTION update_language_contribution()
RETURNS void AS $$
DECLARE
 rec_contribution   RECORD;
 cur_contributions CURSOR
 FOR SELECT *
 FROM contribution;
BEGIN
   -- Open the cursor
   OPEN cur_contributions;

   LOOP
      FETCH cur_contributions INTO rec_contribution;
    -- exit when no more row to fetch
      EXIT WHEN NOT FOUND;
      RAISE NOTICE '%', rec_contribution.contribution_id;

	EXECUTE format('UPDATE contribution SET lang = (select u.language from appcivist_user u, contribution_appcivist_user cu ' ||
      'where cu.contribution_contribution_id = $1 and cu.appcivist_user_user_id = u.user_id limit 1) where contribution_id = $1')
      USING rec_contribution.contribution_id;
   END LOOP;

   CLOSE cur_contributions;

END; $$

LANGUAGE plpgsql;

select update_language_contribution();

UPDATE contribution SET lang = 'en' where lang is NULL;

UPDATE contribution 
SET document = to_tsvector(contribution.lang::regconfig, unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

# --- !Downs