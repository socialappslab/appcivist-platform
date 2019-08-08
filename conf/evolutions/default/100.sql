# --- !Ups

CREATE OR REPLACE FUNCTION generate_proposal_source_codes
  (campaignID bigint, replace_existing_codes boolean) RETURNS void AS $$
DECLARE

BEGIN
  raise notice 'Generating source codes for campaig = % ...', campaignID;

  IF replace_existing_codes THEN
    UPDATE contribution co
    SET source_code =  
      CASE 
          WHEN c.status in ('PUBLISHED', 'DRAFT', 'EXCLUDED', 'ARCHIVED', 'NEW', 'PUBLIC_DRAFT', 'INBALLOT') and c.parent_id is null then 'P'||c.contribution_id 
          WHEN c.status not in ('FORKED_PUBLISHED') and c.parent_id is not null then 'P'||c.parent_id
          ELSE 'P'||c.contribution_id||
              substring('abcdefghijklmnopqrstuvwxyz'
                from ( (select count(*) from contribution c2 where c2.contribution_id<c.contribution_id and c2.parent_id = c.parent_id and c2.status = 'FORKED_PUBLISHED')::integer + 1 ) 
                  for 1
              )
      END
    FROM contribution c
    JOIN resource_space_contributions rsc on c.contribution_id = rsc.contribution_contribution_id 
    JOIN campaign ca on rsc.resource_space_resource_space_id = ca.resources_resource_space_id 
    WHERE ca.campaign_id = campaignID and co.contribution_id = c.contribution_id;
  ELSE
    UPDATE contribution co
    SET source_code =  
      CASE 
          WHEN c.status in ('PUBLISHED', 'DRAFT', 'EXCLUDED', 'ARCHIVED', 'NEW', 'PUBLIC_DRAFT', 'INBALLOT') and c.parent_id is null then 'P'||c.contribution_id 
          WHEN c.status not in ('FORKED_PUBLISHED') and c.parent_id is not null then 'P'||c.parent_id
          ELSE 'P'||c.contribution_id||
              substring('abcdefghijklmnopqrstuvwxyz'
                from ( (select count(*) from contribution c2 where c2.contribution_id<c.contribution_id and c2.parent_id = c.parent_id and c2.status = 'FORKED_PUBLISHED')::integer + 1 ) 
                  for 1
              )
      END
    FROM contribution c
    JOIN resource_space_contributions rsc on c.contribution_id = rsc.contribution_contribution_id 
    JOIN campaign ca on rsc.resource_space_resource_space_id = ca.resources_resource_space_id 
    WHERE ca.campaign_id = campaignID and co.contribution_id = c.contribution_id and c.source_code is null;
  END IF;
END;
$$ LANGUAGE plpgsql;

# --- !Downs
DROP FUNCTION generate_proposal_source_codes (campaignID bigint, replace_existing_codes boolean) ;
