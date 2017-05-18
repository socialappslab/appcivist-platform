# --- !Ups
ALTER TABLE non_member_author ADD COLUMN publishContact BOOLEAN DEFAULT FALSE;
ALTER TABLE non_member_author ADD COLUMN subscribed BOOLEAN DEFAULT FALSE;
ALTER TABLE non_member_author ADD COLUMN Phone varchar(30) DEFAULT '';

# --- !Downs
ALTER TABLE non_member_author DROP COLUMN publishContact;
ALTER TABLE non_member_author DROP COLUMN subscribed;
ALTER TABLE non_member_author DROP COLUMN Phone;
