# --- !Ups

ALTER TABLE public.working_group ADD COLUMN creator_user_id bigint;
ALTER TABLE public.working_group ADD CONSTRAINT fk_creator_wk FOREIGN KEY (creator_user_id) REFERENCES public.appcivist_user (user_id) MATCH SIMPLE;
ALTER TABLE public.campaign ADD COLUMN creator_user_id bigint;
ALTER TABLE public.campaign ADD CONSTRAINT fk_creator_campaign FOREIGN KEY (creator_user_id) REFERENCES public.appcivist_user (user_id) MATCH SIMPLE;

# --- !Downs
