# --- !Ups

ALTER TABLE location ADD COLUMN additional_info TEXT;
ALTER TABLE location ADD COLUMN best_coordinates integer;
ALTER TABLE location ADD COLUMN source character varying(255) default 'Open Street Map';
ALTER TABLE location ADD COLUMN marked_for_review boolean default false;

CREATE TABLE working_group_location (
  working_group_group_id bigint,
  location_location_id bigint,
  CONSTRAINT pk_working_group_location PRIMARY KEY (working_group_group_id, location_location_id),
  CONSTRAINT fk_group_id FOREIGN KEY (working_group_group_id)
  REFERENCES public.working_group (group_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_location_id FOREIGN KEY (location_location_id)
  REFERENCES public.location (location_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

update location set marked_for_review = true;

# --- !Downs
