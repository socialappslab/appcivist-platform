# --- !Ups
alter table contribution add column plain_text text;
update contribution set plain_text = text;

# --- Downs