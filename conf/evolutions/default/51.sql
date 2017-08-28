-- 51. SQL
alter table subscription drop column user_user_id;
alter table subscription add column user_id character varying(40);

alter table subscription drop column space_id;
alter table subscription add column space_id character varying(40);