ALTER TABLE location ADD COLUMN additional_info TEXT;
ALTER TABLE location ADD COLUMN best_coordinates integer;

CREATE TABLE working_group_location (
  working_group_group_id bigint,
  location_location_id bigint,
  CONSTRAINT pk_working_group_location PRIMARY KEY (group_id, location_id),
  CONSTRAINT fk_group_id FOREIGN KEY (group_id)
  REFERENCES public.working_group (group_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_location_id FOREIGN KEY (location_id)
  REFERENCES public.location (location_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);