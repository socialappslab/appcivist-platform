CREATE TEXT SEARCH CONFIGURATION "es-py" ( COPY = spanish );
ALTER TEXT SEARCH CONFIGURATION "es-py" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, spanish_stem;
