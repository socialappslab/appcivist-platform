ALTER TABLE contribution_jury DROP CONSTRAINT fk_contribution_jury_user;
ALTER TABLE contribution_jury DROP COLUMN user_id;
ALTER TABLE contribution_jury ADD COLUMN user_id bigint;
alter table contribution_jury add constraint fk_contribution_jury_user foreign key (user_id) references appcivist_user (user_id);
ALTER TABLE contribution_jury ADD COLUMN username varchar(100);