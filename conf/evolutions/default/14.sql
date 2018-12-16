# --- !Ups
ALTER TABLE non_member_author ADD COLUMN gender varchar(30);
ALTER TABLE non_member_author ADD COLUMN age smallint;

# --- !Downs
ALTER TABLE non_member_author DROP COLUMN gender;
ALTER TABLE non_member_author DROP COLUMN age;
