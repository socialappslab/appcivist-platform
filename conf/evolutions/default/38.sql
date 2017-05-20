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

alter table non_member_author add column lang varchar(255);
alter table non_member_author add column creation timestamp;
alter table non_member_author add column last_update timestamp;
alter table non_member_author add column removal timestamp;
alter table non_member_author add column removed boolean;

# --- !Downs