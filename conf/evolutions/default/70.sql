# --- !Ups
CREATE TEXT SEARCH CONFIGURATION "pt-br" ( COPY = portuguese );
ALTER TEXT SEARCH CONFIGURATION "pt-br" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, portuguese_stem;

CREATE TEXT SEARCH CONFIGURATION "fr-ca" ( COPY = french );
ALTER TEXT SEARCH CONFIGURATION "fr-ca" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, french_stem;

CREATE TEXT SEARCH CONFIGURATION "de-de" ( COPY = german );
ALTER TEXT SEARCH CONFIGURATION "de-de" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, german_stem;

CREATE TEXT SEARCH CONFIGURATION "pt" ( COPY = portuguese );
ALTER TEXT SEARCH CONFIGURATION "pt" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, portuguese_stem;

CREATE TEXT SEARCH CONFIGURATION "de" ( COPY = german );
ALTER TEXT SEARCH CONFIGURATION "de" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, german_stem;