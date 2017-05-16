ALTER TABLE contribution ADD COLUMN source VARCHAR;
ALTER TABLE contribution ADD COLUMN source_url TEXT;
ALTER TABLE non_member_author ADD COLUMN source VARCHAR;
ALTER TABLE non_member_author ADD COLUMN source_url TEXT;