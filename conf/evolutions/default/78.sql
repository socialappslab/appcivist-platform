# --- !Ups

UPDATE public.contribution SET comment_count = 0 WHERE comment_count < 0;

# --- !Downs
