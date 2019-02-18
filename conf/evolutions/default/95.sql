# --- !Ups

ALTER TABLE working_group_profile add COLUMN auto_accept_membership boolean default true;

# --- !Downs
