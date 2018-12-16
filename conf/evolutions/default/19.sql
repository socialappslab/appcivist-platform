# --- !Ups
alter table log add column remote_host varchar;
alter table log add column comment varchar;
alter table contribution add column forum_resource_space_id integer;

ALTER TABLE contribution
  ADD CONSTRAINT fk_contribution_forum FOREIGN KEY (forum_resource_space_id)
      REFERENCES public.resource_space (resource_space_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

# --- !Downs
