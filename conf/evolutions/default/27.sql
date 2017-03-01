# --- !CommentCount
alter table contribution add column comment_count integer;
alter table contribution add column forum_comment_count integer;
update contribution set comment_count=0;
update contribution set forum_comment_count=0;