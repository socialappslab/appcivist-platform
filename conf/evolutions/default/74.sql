-- !Ups

-- Add or update configs in an assenbly
CREATE OR REPLACE FUNCTION public.create_add_config_to_assembly (config_key VARCHAR, config_value VARCHAR, target_assembly_shortname VARCHAR)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  now_date TIMESTAMP;
  target_assembly_rs_id BIGINT;
  target_assembly_uuid VARCHAR;
  new_config_uuid VARCHAR;
  existing_config RECORD;
  target_assembly_lang VARCHAR;

BEGIN
  SELECT now() INTO now_date;

  -- get assembly target uuid
  SELECT resources_resource_space_id, uuid
  FROM assembly
  WHERE shortname = target_assembly_shortname
  INTO target_assembly_rs_id, target_assembly_uuid, target_assembly_lang;

  RAISE NOTICE 'Checking if config (%) already exists for in RS (%) of assembly (%, %)',
  config_key, target_assembly_rs_id, target_assembly_shortname, target_assembly_uuid;
  SELECT c.* FROM config c, resource_space_config rs
  WHERE rs.resource_space_resource_space_id = target_assembly_rs_id
        AND c.key = config_key
        AND rs.config_uuid = c.uuid
  INTO existing_config;

  RAISE NOTICE  'Existing Config => %', existing_config;


  IF (existing_config.key = config_key) THEN
    -- UPDATE value of config
    RAISE NOTICE 'Config (%) already exists. Updating its value (% => %)', existing_config.key, existing_config.value, config_value;
    UPDATE config SET value = config_value WHERE uuid = existing_config.uuid;
  ELSE
    -- INSERT config and ASSOCIATE to assembly
    RAISE NOTICE 'Processing config (%,%)', config_key, config_value;
    -- create new config record
    INSERT INTO "public"."config"
    (   "creation", "last_update", "lang", "removed",
        "key", "value", "config_target", "target_uuid")
    VALUES
      (   now_date, now_date, target_assembly_lang, 'FALSE',
          config_key, config_value, 'ASSEMBLY', target_assembly_uuid)
    RETURNING "uuid" INTO new_config_uuid;

    RAISE NOTICE 'Associating (config, %) to (assembly, rs id) => (%,%)',
    new_config_uuid, target_assembly_shortname, target_assembly_rs_id;
    -- Associate config to target assembly
    INSERT INTO "public"."resource_space_config"
    ("resource_space_resource_space_id", "config_uuid")
    VALUES (target_assembly_rs_id, new_config_uuid);
    RAISE NOTICE 'Config added!';
  END IF;
END

$$;
ALTER FUNCTION create_add_config_to_assembly (config_key VARCHAR, config_value VARCHAR, target_assembly_shortname VARCHAR) OWNER TO appcivist;

-- !Downs