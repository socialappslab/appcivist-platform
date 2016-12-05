# --- !Ups
ALTER TABLE component ADD COLUMN type varchar(30) DEFAULT 'IDEAS';

# --- !Downs
ALTER TABLE component DROP COLUMN type;
