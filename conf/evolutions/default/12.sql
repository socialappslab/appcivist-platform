# --- !Ups
ALTER TABLE theme ADD COLUMN type varchar(255) DEFAULT 'EMERGENT';
ALTER TABLE user_profile ADD COLUMN phone varchar(30);
ALTER TABLE user_profile ADD COLUMN note text;
ALTER TABLE user_profile ADD COLUMN gender varchar(30);

# --- !Downs
ALTER TABLE theme DROP COLUMN type;
ALTER TABLE user_profile DROP COLUMN phone;
ALTER TABLE user_profile DROP COLUMN note;
ALTER TABLE user_profile DROP COLUMN gender;