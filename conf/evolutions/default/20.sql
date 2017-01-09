# --- !Ups

-- Created by @cdparra
-- Creates resource spaces for entities that don't have them
-- */
create extension "uuid-ossp";

CREATE OR REPLACE FUNCTION create_missing_resource_spaces
(
    entity varchar, 
    field varchar, 
    raise_notice boolean DEFAULT false
)
RETURNS void AS
$BODY$
DECLARE
    r RECORD;;
    raise_exception BOOLEAN;;
    new_resource_space_id BIGINT;;
    new_uuid VARCHAR;;
    id_field VARCHAR;;
    r_id_field BIGINT;;
    
BEGIN
    raise_exception = true;;
    
    IF ( lower(entity) = 'campaign') THEN   
        IF ( lower(field) = 'resources_resource_space_id') THEN   
            raise_exception = false;;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;;    
        END IF;;
    ELSIF ( lower(entity) = 'contribution') THEN  
        IF ( lower(field) = 'resource_space_resource_space_id') THEN   
            raise_exception = false;;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;;    
        END IF;;
    ELSIF ( lower(entity) = 'assembly') THEN
        IF ( lower(field) = 'resources_resource_space_id') THEN   
            raise_exception = false;;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;;    
        END IF;;
    ELSIF ( lower(entity) = 'working_group') THEN
        IF ( lower(field) = 'resources_resource_space_id') THEN   
            raise_exception = false;;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;;    
        END IF;;
    ELSIF ( lower(entity) = 'component') THEN
        IF ( lower(field) = 'resource_space_resource_space_id') THEN   
            raise_exception = false;;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;;    
        END IF;;
    END IF;;

    IF (raise_exception) THEN 
        RAISE EXCEPTION 'The entity % and field % are not valid', entity, field;;
    END IF;;

    FOR r IN EXECUTE format('SELECT * FROM %s WHERE %s is null', lower(entity), lower(field))
    LOOP
   
        IF (raise_notice) THEN
            IF ( lower(entity) = 'campaign' OR lower(entity) = 'component' OR lower(entity) = 'contribution' ) THEN   
                RAISE NOTICE 'Creating resource space for %: %', lower(entity),r.title;;
            ELSE
                RAISE NOTICE 'Creating resource space for %: %', lower(entity),r.name;;
            END IF;;
        END IF;;
        
        SELECT uuid_generate_v4() into new_uuid;;
        
        INSERT INTO resource_space (
            creation, last_update, lang, removed, 
            uuid, type)
        VALUES ( 
            now(), now(), 'en-US', FALSE, 
            new_uuid, upper(entity))
        RETURNING resource_space.resource_space_id INTO new_resource_space_id;;
           
        IF ( lower(entity) = 'campaign') THEN   
            -- UPDATE campaign SET resources_resource_space_id = new_resource_space_id WHERE campaign_id = r.campaign_id ;;
            id_field = 'campaign_id';;
            r_id_field = r.campaign_id;;
        ELSIF ( lower(entity) = 'contribution') THEN  
            -- UPDATE contribution SET resource_space_resource_space_id = new_resource_space_id WHERE contribution_id = r.contribution_id ;;
            id_field = 'contribution_id';;
            r_id_field = r.contribution_id;;
        ELSIF ( lower(entity) = 'assembly') THEN
            -- UPDATE assembly SET resources_resource_space_id = new_resource_space_id WHERE assembly_id = r.assembly_id;;
            id_field = 'assembly_id';;
            r_id_field = r.assembly_id;;
        ELSIF ( lower(entity) = 'working_group') THEN
            -- UPDATE working_group SET resources_resource_space_id = new_resource_space_id WHERE working_group_id = r.working_group_id;;
            id_field = 'working_group_id';;
            r_id_field = r.working_group_id;;
        ELSIF ( lower(entity) = 'component') THEN
            -- UPDATE component SET resource_space_resource_space_id = new_resource_space_id WHERE component_id = r.component_id;;
            id_field = 'component_id';;
            r_id_field = r.component_id;;
        END IF;;
                
        
        IF (raise_notice) THEN
            RAISE NOTICE 'Updating % by adding new resource space with id %', lower(entity),new_resource_space_id;;
            RAISE NOTICE 'UPDATE % SET % = % WHERE % = %',lower(entity), lower(field), new_resource_space_id, id_field, r_id_field;;
        END IF;;
        EXECUTE format('UPDATE %s SET %s = %L WHERE %s = %L',lower(entity), lower(field), new_resource_space_id, id_field, r_id_field);;
    END LOOP;;
END;;
$BODY$
LANGUAGE plpgsql VOLATILE;

# --- !Downs
drop function create_missing_resource_spaces;