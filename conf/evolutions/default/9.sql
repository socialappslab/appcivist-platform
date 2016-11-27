# --- !Ups

-- Adding ON DELETE CASCADE to items in resource spaces to be able to delete them and their associations 
-- to the resource space without problems and without deleting the resource space itself
ALTER TABLE resource_space_resource
  DROP CONSTRAINT fk_resource_space_resource_re_02;

ALTER TABLE resource_space_resource
  ADD CONSTRAINT fk_resource_space_resource_re_02 FOREIGN KEY (resource_resource_id)
      REFERENCES resource (resource_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
      
# --- !Downs