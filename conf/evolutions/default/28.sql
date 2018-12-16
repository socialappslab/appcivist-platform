# --- !Ups

alter table contribution add column total_comments integer;
update contribution set total_comments=0;

# --- !Ups
