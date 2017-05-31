# --- !Ups
-- Execute endpoint first
-- POST       /api/contribution/language

UPDATE contribution 
SET document = to_tsvector(contribution.lang::regconfig, unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

# --- !Downs