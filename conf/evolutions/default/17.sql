# --- !Ups
alter table log add column remote_address varchar(255);

# --- !Downs
alter table log drop column remote_address; 