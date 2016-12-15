# --- !Ups
alter table contribution add column popularity integer;
alter table contribution add column pinned boolean default false;

# --- !Downs
alter table drop column popularity;
alter table drop column pinned;