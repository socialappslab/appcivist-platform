# --- !Ups
alter table contribution_feedback add column non_member_author_id bigint;
alter table contribution_feedback add constraint fk_non_member_author_feedback foreign key (non_member_author_id) references non_member_author(id);
ALTER TABLE contribution_feedback ALTER COLUMN user_id DROP NOT NULL;
# --- Downs