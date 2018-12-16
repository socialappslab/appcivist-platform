# --- !Ups


ALTER TABLE "public"."campaign" ADD COLUMN location_location_id bigint;
ALTER TABLE "public"."campaign" ADD CONSTRAINT fk_campaign_location FOREIGN KEY (location_location_id)
      REFERENCES location (location_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX ix_campaign_location
  ON campaign
  USING btree
  (location_location_id);

# --- !Downs
