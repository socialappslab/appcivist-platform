# --- !Ups
-- Helper functions to speedup creation of some elements

-- Helper random string
Create or replace function random_string(length integer) returns text as
$$
declare
  chars text[] := '{0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z}';
  result text := '';
  i integer := 0;
begin
  if length < 0 then
    raise exception 'Given length cannot be less than 0';
  end if;
  for i in 1..length loop
    result := result || chars[1+random()*(array_length(chars, 1)-1)];
  end loop;
  return result;
end;
$$ language plpgsql;
ALTER FUNCTION random_string(length integer) OWNER TO appcivist;

ALTER TABLE "public"."campaign" ALTER COLUMN "shortname" SET DEFAULT random_string(8);
ALTER TABLE "public"."campaign" ADD UNIQUE ("shortname");
ALTER TABLE "public"."campaign" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."resource_space" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component_milestone" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."custom_field_value_option" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
UPDATE "public"."campaign" SET "shortname" = random_string(8);


-- object: public.component_milestone_component_milestone_id_seq | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.component_milestone_component_milestone_id_seq CASCADE;
CREATE SEQUENCE public.component_component_id_seq
    INCREMENT BY 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    START WITH 1
    CACHE 1
    NO CYCLE;

ALTER TABLE "public"."component" ALTER COLUMN "component_id" SET DEFAULT nextval('component_component_id_seq'::regclass);

alter sequence component_component_id_seq OWNED BY component.component_id;

-- Create a new campaign under an assembly
CREATE OR REPLACE FUNCTION public.create_new_campaign
	( assemblyid BIGINT, title VARCHAR,  shortname VARCHAR, goal VARCHAR,  logo TEXT, cover TEXT, lng VARCHAR)
	RETURNS void
	LANGUAGE plpgsql
	VOLATILE 
	CALLED ON NULL INPUT
	SECURITY INVOKER
	COST 100
	AS $$

DECLARE
	logo_id BIGINT;
    cover_id BIGINT;
    campaign_id BIGINT;
	
	logo_uuid VARCHAR;
    cover_uuid VARCHAR;
    campaign_uuid VARCHAR;

    campaign_rs_id BIGINT;
    campaign_forum_rs_id BIGINT;
    
	assembly_rs_id BIGINT;
    now_date TIMESTAMP;
BEGIN
    SELECT now() into now_date;

	-- logo resource
	INSERT INTO "public"."resource" ("creation", "last_update", "lang", "removed", "url", "resource_type") 
	VALUES (now_date, now_date, lng, 'FALSE', logo, 'PICTURE') 
	RETURNING "resource_id", "uuid" into logo_id, logo_uuid;
	RAISE NOTICE 'Resource for logo created (id, uuid) = (%,%): ', logo_id, logo_uuid;
	
	-- cover resource
	INSERT INTO "public"."resource" ("creation", "last_update", "lang", "removed", "url", "resource_type") 
	VALUES (now_date, now_date, lng, 'FALSE', cover, 'PICTURE') 
	RETURNING "resource_id", "uuid" into cover_id, cover_uuid;
	RAISE NOTICE 'Resource for cover created (id, uuid) = (%,%): ', cover_id, cover_uuid;

    -- campaign resource spaces
	INSERT INTO "public"."resource_space"("creation", "last_update", "lang", "removed", "type") 
	VALUES (now_date, now_date, lng, 'FALSE', 'CAMPAIGN') 
	RETURNING "resource_space_id" into campaign_rs_id;
	RAISE NOTICE 'Resource Space for campaign created (id) = (%): ', campaign_rs_id;

	INSERT INTO "public"."resource_space"("creation", "last_update", "lang", "removed", "type") 
	VALUES (now_date, now_date, lng, 'FALSE', 'CAMPAIGN') 
	RETURNING "resource_space_id" into campaign_forum_rs_id;
	RAISE NOTICE 'Public Resource Space for campaign created (id) = (%): ', campaign_forum_rs_id;

    -- create campaign
	INSERT INTO "public"."campaign"
		("creation", "last_update", "lang", "removed", "title", "shortname", "goal", "listed", "template_campaign_template_id", "cover_resource_id", "logo_resource_id", "resources_resource_space_id", "forum_resource_space_id") 
	VALUES
		(now_date, now_date, lng, 'FALSE', title, shortname, goal, 'TRUE', 1, cover_id, logo_id, campaign_rs_id, campaign_forum_rs_id) 
	RETURNING "campaign"."campaign_id", "campaign"."uuid" into campaign_id, campaign_uuid;
	RAISE NOTICE 'Campaign created (id, uuid) = (%,%): ', campaign_id, campaign_uuid;

	-- get assembly 
	SELECT resources_resource_space_id into assembly_rs_id from assembly where assembly_id = assemblyid;
	RAISE NOTICE 'Adding Campaign to resource space (%) of assembly (%): ', assembly_rs_id, assemblyid;

	-- insert campaign on assembly resource space
	INSERT INTO "public"."resource_space_campaign" (resource_space_resource_space_id, campaign_campaign_id)
 	VALUES (assembly_rs_id, campaign_id);
 
END

$$;
ALTER FUNCTION create_new_campaign( assemblyid BIGINT, title VARCHAR,  shortname VARCHAR, goal VARCHAR,  logo TEXT, cover TEXT, lng VARCHAR) OWNER TO appcivist;

-- Create a new theme and add it to a campaign
CREATE OR REPLACE FUNCTION public.create_theme_and_add_to_campaign (campaign_shortname VARCHAR, theme_title VARCHAR,  theme_description VARCHAR, theme_type VARCHAR, theme_lang VARCHAR)
    RETURNS void
    LANGUAGE plpgsql
    VOLATILE 
    CALLED ON NULL INPUT
    SECURITY INVOKER
    COST 100
    AS $$

DECLARE
    new_theme_id BIGINT;
    now_date TIMESTAMP;
    campaign_rs_id BIGINT;
BEGIN
    SELECT now() INTO now_date;

    -- create theme record
    INSERT INTO "public"."theme"
        ("creation", "last_update", "lang", "removed", "title", "description", "type") 
    VALUES
        (now_date, now_date, theme_lang, 'FALSE', theme_title, theme_description, theme_type) 
    RETURNING "theme_id" INTO new_theme_id;
    RAISE NOTICE 'Theme created (id) = (%): ', new_theme_id;

    -- get campaign resource space id
    SELECT "resources_resource_space_id" INTO campaign_rs_id FROM "public"."campaign" WHERE "campaign"."shortname" = campaign_shortname;
    RAISE NOTICE 'Getting resource space id of campaign %: (id) = (%): ', campaign_shortname, campaign_rs_id;

    -- insert theme into campaign resource space
    INSERT INTO "public"."resource_space_theme" ("resource_space_resource_space_id", "theme_theme_id") 
    VALUES (campaign_rs_id, new_theme_id);
    RAISE NOTICE 'Campaign added!';

END
$$;
ALTER FUNCTION create_theme_and_add_to_campaign (campaign_shortname VARCHAR, theme_title VARCHAR,  theme_description VARCHAR, theme_type VARCHAR, theme_lang VARCHAR) OWNER TO appcivist;

-- Read a campaign list of components, ordered by position, and create a timeline graph
CREATE OR REPLACE FUNCTION public.generate_timeline_edges (target_campaign_shortname VARCHAR)
    RETURNS void
    LANGUAGE plpgsql
    VOLATILE 
    CALLED ON NULL INPUT
    SECURITY INVOKER
    COST 100
    AS $$

DECLARE
    now_date TIMESTAMP;
    source_component RECORD;
    target_component RECORD;
    target_campaign_rs_id BIGINT;
    target_campaign_id BIGINT;
    target_campaign_lang VARCHAR;
    source_is_start BOOLEAN;
    position_counter INT := 0;

BEGIN
    SELECT now() INTO now_date;

    -- get resource space id of campaign
    SELECT campaign_id, resources_resource_space_id, lang FROM campaign WHERE shortname = target_campaign_shortname
    INTO target_campaign_id, target_campaign_rs_id, target_campaign_lang;
    
    FOR target_component IN
        SELECT * FROM component c, resource_space_campaign_components rs 
        WHERE rs.resource_space_resource_space_id = target_campaign_rs_id 
            AND c.component_id = rs.component_component_id
            AND timeline = 1
        ORDER BY position ASC
    LOOP
        -- if target is position start, make it source and go next, put source_is_start to true
        IF (position_counter = 0) THEN
            position_counter := position_counter + 1;
            source_component := target_component; 
            RAISE NOTICE 'Starting component => %, %', source_component.component_id, source_component.title;
        ELSE 
            RAISE NOTICE 'Creating edge (%) => (%)', source_component.title, target_component.title;

            IF (position_counter =1) THEN
                source_is_start := TRUE;
            ELSE
                source_is_start := FALSE;
            END IF;
            INSERT INTO "public"."campaign_timeline_edge"
                (   "creation", "last_update", "lang", "removed", 
                    "campaign_campaign_id", "start", 
                    "from_component_component_id", "to_component_component_id") 
            VALUES
                (   now_date, now_date, target_campaign_lang, 'FALSE',
                    target_campaign_id, source_is_start, 
                    source_component.component_id, target_component.component_id);
            position_counter := position_counter + 1;
            source_component := target_component; 
        END IF;
        
    END LOOP;
END

$$;
ALTER FUNCTION generate_timeline_edges(target_campaign_shortname VARCHAR) OWNER TO appcivist;

-- Copy components and milestones from one campaign to the other, shifting dates from a starting point
CREATE OR REPLACE FUNCTION public.copy_components_and_milestones_from_campaign
    (   source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR,
        new_start_date TIMESTAMP
    )
    RETURNS void
    LANGUAGE plpgsql
    VOLATILE 
    CALLED ON NULL INPUT
    SECURITY INVOKER
    COST 100
    AS $$

DECLARE
    now_date TIMESTAMP;
    source_campaign_start_date TIMESTAMP;
    shift_interval INTERVAL;

    source_campaign_rs_id BIGINT;
    target_campaign_rs_id BIGINT;


    new_component RECORD;
    new_milestone RECORD;

    new_component_id BIGINT;
    new_component_rs_id BIGINT;
    
    new_milestone_id BIGINT;
    new_milestone_rs_id BIGINT;

BEGIN
    SELECT now() INTO now_date;

    -- get resource space id of campaign
    SELECT resources_resource_space_id FROM campaign WHERE shortname = source_campaign_shortname
    INTO source_campaign_rs_id;

    -- get starting date of campaign
    SELECT min(c.start_date) FROM component c, resource_space_campaign_components rs 
    WHERE rs.resource_space_resource_space_id = source_campaign_rs_id 
        AND c.component_id = rs.component_component_id
    INTO source_campaign_start_date;
    RAISE NOTICE 'Start date of the source campaign = (%): ', source_campaign_start_date;

    SELECT new_start_date - source_campaign_start_date INTO shift_interval;  
    RAISE NOTICE 'Shifting interval based on new start date = (%): ', shift_interval;

    -- create component record
    SELECT new_start_date - source_campaign_start_date INTO shift_interval;  

    FOR new_component IN
        SELECT * FROM component c, resource_space_campaign_components rs 
        WHERE rs.resource_space_resource_space_id = source_campaign_rs_id 
            AND c.component_id = rs.component_component_id
    LOOP
        RAISE NOTICE 'Processing component (%,%,%) => %', 
            new_component .title, new_component .key, new_component .start_date, 
            new_component.start_date + shift_interval;

        -- Create resource space for new component
        INSERT INTO "public"."resource_space"
            ("creation", "last_update", "lang", "removed", "type") 
        VALUES 
            (now_date, now_date, new_component.lang, 'FALSE', 'COMPONENT') 
        RETURNING "resource_space_id" into new_component_rs_id;
        RAISE NOTICE 'Resource Space for component created (id) = (%): ', new_component_rs_id;

        -- Create new component record
        INSERT INTO "public"."component"
            (   "creation", "last_update", "lang", "removed", 
                "title", "description", "key", "type", 
                "start_date", "end_date", 
                "position", "timeline", 
                "resource_space_resource_space_id") 
        VALUES
            (   now_date, now_date, new_component.lang, 'FALSE', 
                new_component.title, new_component.description, new_component.key, new_component.type, 
                new_component.start_date + shift_interval, new_component.end_date + shift_interval,
                new_component.position, new_component.timeline,
                new_component_rs_id
            )
        RETURNING "component_id" INTO new_component_id;

        -- read target campaign resource space
        SELECT resources_resource_space_id INTO target_campaign_rs_id
        FROM campaign WHERE shortname = target_campaign_shortname;

        -- Associate component to target campaign
        INSERT INTO "public"."resource_space_campaign_components" 
            ("resource_space_resource_space_id", "component_component_id") 
        VALUES (target_campaign_rs_id, new_component_id);
        RAISE NOTICE 'Component added!';

        -- Copy milestones on component
        FOR new_milestone IN
            SELECT * FROM component_milestone c, resource_space_campaign_milestones rs 
            WHERE rs.resource_space_resource_space_id = new_component.resource_space_resource_space_id
                AND c.component_milestone_id = rs.component_milestone_component_milestone_id
        LOOP
            RAISE NOTICE 'Processing milestone (%,%,%) => %', 
                new_milestone.title, new_milestone.key, new_milestone.date,
                new_milestone.date+shift_interval;

            -- Create a copy of the milestone space for new component
            INSERT INTO "public"."component_milestone"
                (   "creation", "last_update", "lang", "removed", 
                    "title", "description", "key", "position", "type", "main_contribution_type",
                    "date", "days") 
            VALUES
                (   now_date, now_date, new_milestone.lang, 'FALSE',
                    new_milestone.title, new_milestone.description, new_milestone.key, new_milestone.position, new_milestone.type, new_milestone.main_contribution_type,
                    new_milestone.date+shift_interval, new_milestone.days
                )
            RETURNING "component_milestone_id" into new_milestone_id;

            -- insert milestone on component resource space 
            INSERT INTO "public"."resource_space_campaign_milestones" 
                ("resource_space_resource_space_id", "component_milestone_component_milestone_id") 
            VALUES (new_component_rs_id, new_milestone_id);
            RAISE NOTICE 'New Milestone Added!';
        END LOOP;   
    END LOOP;
END

$$;
ALTER FUNCTION copy_components_and_milestones_from_campaign(   source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR, new_start_date TIMESTAMP) OWNER TO appcivist;

-- Copy configs from one campaign to the other
CREATE OR REPLACE FUNCTION public.copy_configs_from_campaign (source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR)
    RETURNS void
    LANGUAGE plpgsql
    VOLATILE 
    CALLED ON NULL INPUT
    SECURITY INVOKER
    COST 100
    AS $$

DECLARE
    now_date TIMESTAMP;
    source_campaign_rs_id BIGINT;
    target_campaign_rs_id BIGINT;
    target_campaign_uuid VARCHAR;
    config_to_copy RECORD;
    new_config_uuid VARCHAR;

BEGIN
    SELECT now() INTO now_date;

    -- get resource space id of campaign
    SELECT resources_resource_space_id 
    FROM campaign 
    WHERE shortname = source_campaign_shortname
    INTO source_campaign_rs_id;

    -- get campaign target uuid
    SELECT resources_resource_space_id, uuid 
    FROM campaign 
    WHERE shortname = target_campaign_shortname
    INTO target_campaign_rs_id, target_campaign_uuid;

    FOR config_to_copy IN
        SELECT * FROM config c, resource_space_config rs 
        WHERE rs.resource_space_resource_space_id = source_campaign_rs_id 
            AND c.uuid = rs.config_uuid
    LOOP
        RAISE NOTICE 'Processing config (%,%)', 
            config_to_copy.key, config_to_copy.value;
        -- create new config record
        INSERT INTO "public"."config"
            (   "creation", "last_update", "lang", "removed", 
                "key", "value", "config_target", "target_uuid", "definition_uuid" ) 
        VALUES
            (   now_date, now_date, config_to_copy.lang, 'FALSE', 
                config_to_copy.key, config_to_copy.value, config_to_copy.config_target, target_campaign_uuid, config_to_copy.definition_uuid)
        RETURNING "uuid" INTO new_config_uuid;

        
        RAISE NOTICE 'Associatign (config, %) to (campaign, rs id) => (%,%)', 
            uuid_generate_v4(), target_campaign_shortname, target_campaign_rs_id;
        -- Associate config to target campaign
        INSERT INTO "public"."resource_space_config" 
            ("resource_space_resource_space_id", "config_uuid") 
        VALUES (target_campaign_rs_id, new_config_uuid);
        RAISE NOTICE 'Config added!';
    END LOOP;
END

$$;
ALTER FUNCTION copy_configs_from_campaign(source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR) OWNER TO appcivist;

-- Copy components and milestones from one campaign to the other, shifting dates from a starting point
CREATE OR REPLACE FUNCTION public.create_add_config_to_campaign (config_key VARCHAR, config_value VARCHAR, target_campaign_shortname VARCHAR)
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
    target_campaign_uuid VARCHAR;
    new_config_uuid VARCHAR;
    existing_config RECORD;
    target_campaign_lang VARCHAR;

BEGIN
    SELECT now() INTO now_date;

    -- get campaign target uuid
    SELECT resources_resource_space_id, uuid 
    FROM campaign 
    WHERE shortname = target_campaign_shortname
    INTO target_campaign_rs_id, target_campaign_uuid, target_campaign_lang;

    RAISE NOTICE 'Checking if config (%) already exists for in RS (%) of campaign (%, %)', 
        config_key, target_campaign_rs_id, target_campaign_shortname, target_campaign_uuid;
    SELECT c.* FROM config c, resource_space_config rs 
        WHERE rs.resource_space_resource_space_id = target_campaign_rs_id 
            AND c.key = config_key
            AND rs.config_uuid = c.uuid         
    INTO existing_config;

    RAISE NOTICE  'Existing Config => %', existing_config; 


    IF (existing_config.key = config_key) THEN
        -- UPDATE value of config
        RAISE NOTICE 'Config (%) already exists. Updating its value (% => %)', existing_config.key, existing_config.value, config_value; 
        UPDATE config SET value = config_value WHERE uuid = existing_config.uuid;
    ELSE
    -- INSERT config and ASSOCIATE to campaign
        RAISE NOTICE 'Processing config (%,%)', config_key, config_value;
        -- create new config record
        INSERT INTO "public"."config"
            (   "creation", "last_update", "lang", "removed", 
                "key", "value", "config_target", "target_uuid") 
        VALUES
            (   now_date, now_date, target_campaign_lang, 'FALSE', 
                config_key, config_value, 'CAMPAIGN', target_campaign_uuid)
        RETURNING "uuid" INTO new_config_uuid;
    
        RAISE NOTICE 'Associating (config, %) to (campaign, rs id) => (%,%)', 
            new_config_uuid, target_campaign_shortname, target_campaign_rs_id;
        -- Associate config to target campaign
        INSERT INTO "public"."resource_space_config" 
            ("resource_space_resource_space_id", "config_uuid") 
        VALUES (target_campaign_rs_id, new_config_uuid);
        RAISE NOTICE 'Config added!';
    END IF;
END

$$;
ALTER FUNCTION create_add_config_to_campaign (config_key VARCHAR, config_value VARCHAR, target_campaign_shortname VARCHAR) OWNER TO appcivist;

-- Create custom field and add to campaign resource space
CREATE OR REPLACE FUNCTION public.create_add_custom_field_option_value_to_field_definition
    (   target_campaign_shortname VARCHAR, field_name VARCHAR, field_entity_type VARCHAR, 
        new_option VARCHAR, new_option_type VARCHAR, new_option_position INTEGER)
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
    target_field_definition_id BIGINT;
    new_field_value_option_id BIGINT;
    target_lang VARCHAR;

BEGIN
    SELECT now() INTO now_date;
    SELECT resources_resource_space_id INTO target_campaign_rs_id FROM campaign WHERE shortname = target_campaign_shortname;
    
    SELECT cf.custom_field_definition_id, cf.lang 
    INTO target_field_definition_id, target_lang
    FROM resource_space_custom_field_definition rs, custom_field_definition cf
    WHERE rs.resource_space_resource_space_id = target_campaign_rs_id
        AND cf.name = field_name
        AND cf.entity_type = field_entity_type;

    RAISE NOTICE 'Inserting option for field (%,%) on RS (%)', target_field_definition_id, field_name, target_campaign_rs_id;

    INSERT INTO "public"."custom_field_value_option"
        (   "creation", "last_update", "lang", "removed", 
            "value", "value_type", "position", "custom_field_definition_id")
    VALUES 
        (   now_date, now_date, target_lang, 'FALSE',
            new_option, new_option_type, new_option_position, target_field_definition_id)
    RETURNING "custom_field_value_option_id" INTO new_field_value_option_id;

--  RAISE NOTICE 'Created option (%) => %', new_option, new_field_value_option_id;

END

$$;
ALTER FUNCTION create_add_custom_field_option_value_to_field_definition ( target_campaign_shortname VARCHAR, field_name VARCHAR, field_entity_type VARCHAR, new_option VARCHAR, new_option_type VARCHAR, new_option_position INTEGER) OWNER TO appcivist;

-- object: public.create_add_custom_field_to_campaign | type: FUNCTION --
-- DROP FUNCTION IF EXISTS public.create_add_custom_field_to_campaign(character varying,character varying,character varying,text,character varying,character varying,character varying,integer,text,character varying,character varying) CASCADE;
CREATE FUNCTION public.create_add_custom_field_to_campaign ( target_campaign_shortname character varying,  new_field_lang character varying,  new_name character varying,  new_description text,  new_entity_type character varying,  new_entity_filter_attribute_name character varying,  new_entity_filter character varying,  new_field_position integer,  new_field_limit text,  new_field_limit_type character varying,  new_field_type character varying)
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
            "entity_filter", "field_position", "field_limit", "limit_type", "field_type")
    VALUES 
        (   now_date, now_date, new_field_lang, 'FALSE',
            new_name, new_description, new_entity_type, new_entity_filter_attribute_name, 
            new_entity_filter, new_field_position, new_field_limit, new_field_limit_type, new_field_type)
    RETURNING "custom_field_definition_id" INTO new_custom_field_definition_id;

    RAISE NOTICE 'Created custom field (%) => %', new_name, new_custom_field_definition_id;

    INSERT INTO "public"."resource_space_custom_field_definition" 
        ("resource_space_resource_space_id", "custom_field_definition_custom_field_definition_id") 
    VALUES (target_campaign_rs_id, new_custom_field_definition_id);
    RAISE NOTICE 'Custom Field Added! => (%)', new_custom_field_definition_id;
END


$$;
-- ddl-end --
ALTER FUNCTION public.create_add_custom_field_to_campaign(character varying,character varying,character varying,text,character varying,character varying,character varying,integer,text,character varying,character varying) OWNER TO appcivist;
-- ddl-end --





# --- !Downs
