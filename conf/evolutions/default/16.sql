# --- !Ups
alter table contribution add column popularity integer;
alter table contribution add column pinned boolean default false;

# --- !Downs
alter table contribution drop column popularity;
alter table contribution drop column pinned;
