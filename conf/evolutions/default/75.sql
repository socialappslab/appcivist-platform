-- !Ups
CREATE OR REPLACE FUNCTION public.create_add_custom_field_to_campaign ( target_campaign_shortname character varying,  new_field_lang character varying,  new_name character varying,  new_description text,  new_entity_type character varying,  new_entity_filter_attribute_name character varying,  new_entity_filter character varying,  new_field_position integer,  new_field_limit text,  new_field_limit_type character varying,  new_field_type character varying, new_entity_part text)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$


DECLARE
  now_date TIMESTAMP;
  target_campaign_rs_id BIGINT;
  new_custom_field_definition_id BIGINT;

BEGIN
  SELECT now() INTO now_date;
  SELECT resources_resource_space_id INTO target_campaign_rs_id FROM campaign WHERE shortname = target_campaign_shortname;

  INSERT INTO "public"."custom_field_definition"
  (   "creation", "last_update", "lang", "removed",
      "name", "description", "entity_type", "entity_filter_attribute_name",
      "entity_filter", "field_position", "field_limit", "limit_type", "field_type", "entity_part")
  VALUES
    (   now_date, now_date, new_field_lang, 'FALSE',
                  new_name, new_description, new_entity_type, new_entity_filter_attribute_name,
                  new_entity_filter, new_field_position, new_field_limit, new_field_limit_type,
                  new_field_type, new_entity_part)
  RETURNING "custom_field_definition_id" INTO new_custom_field_definition_id;

  RAISE NOTICE 'Created custom field (%) => %', new_name, new_custom_field_definition_id;

  INSERT INTO "public"."resource_space_custom_field_definition"
  ("resource_space_resource_space_id", "custom_field_definition_custom_field_definition_id")
  VALUES (target_campaign_rs_id, new_custom_field_definition_id);
  RAISE NOTICE 'Custom Field Added! => (%)', new_custom_field_definition_id;
END


$$;
-- ddl-end --
ALTER FUNCTION public.create_add_custom_field_to_campaign(character varying,character varying,character varying,text,character varying,character varying,character varying,integer,text,character varying,character varying,text) OWNER TO appcivist;
-- ddl-end --

ALTER TABLE "public"."theme" ADD COLUMN "url" text;



-- !Downs

