# --- !Ups
alter table campaign add column forum_resource_space_id integer;

ALTER TABLE campaign
  ADD CONSTRAINT fk_campaign_forum FOREIGN KEY (forum_resource_space_id)
      REFERENCES public.resource_space (resource_space_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
      
# --- Downs