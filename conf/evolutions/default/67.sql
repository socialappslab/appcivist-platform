CREATE OR REPLACE FUNCTION create_contribution_audits() RETURNS void AS
$BODY$
DECLARE
  r contribution%rowtype;
  cSid bigint;
  campaignId bigint;
  currentComponentId bigint;
  previousComponentId bigint;
  nowDate TIMESTAMP;
  previousComponentEndDate TIMESTAMP;
  previousComponentStartDate TIMESTAMP;
BEGIN
  SELECT now() INTO nowDate;
  FOR r IN
  SELECT c.*
  FROM contribution c, resource_space_contributions rsc, resource_space rs
  WHERE rsc.resource_space_resource_space_id = rs.resource_space_id
        AND rs.type = 'CAMPAIGN'
        AND rsc.contribution_contribution_id = c.contribution_id

  LOOP
    -- 1. Get resource space of the campaign where the contribution was posted
    SELECT rsc.resource_space_resource_space_id
    FROM resource_space_contributions rsc, resource_space rs
    WHERE
      rsc.resource_space_resource_space_id = rs.resource_space_id
      and rs.type = 'CAMPAIGN'
      and rsc.contribution_contribution_id = r.contribution_id
    LIMIT 1
    INTO cSid;

    -- 2. Get the campaign ID related to the resource space
    SELECT c.campaign_id
    FROM campaign c
    WHERE c.resources_resource_space_id = cSid
    INTO campaignId;

    -- 3. Get the component that is current in the campaign timeline
    SELECT co.component_id
    FROM component co, campaign_timeline_edge cte
    WHERE
      co.start_date < nowDate
      and co.end_date > nowDate
      and cte.campaign_campaign_id = campaignId
      and co.component_id = cte.to_component_component_id
    INTO currentComponentId;


    -- 4. Get the component that is previous in the campaign timeline
    SELECT cte.from_component_component_id
    FROM campaign_timeline_edge cte
    WHERE
      cte.to_component_component_id = currentComponentId
      AND cte.campaign_campaign_id = campaignId
    INTO previousComponentId;

    -- 5. Get start and end date of previous component
    SELECT start_date FROM component WHERE component_id = previousComponentId INTO previousComponentStartDate;
    SELECT end_date FROM component WHERE component_id = previousComponentId INTO previousComponentEndDate;

    RAISE NOTICE 'Processing (cont, stat, camp, currComp, prevComp, prevStart, prevEnd => %, %, %, %, %, %, %', r.contribution_id, r.status, campaignId, currentComponentId, previousComponentId, previousComponentStartDate, previousComponentEndDate;
    IF r.status = 'PUBLISHED' AND previousComponentId > 0 THEN
      -- 7. Insert current status, not ended yet
      INSERT INTO public.contribution_status_audit(
        status_start_date, status_end_date, contribution_contribution_id, status)
      VALUES (r.creation, previousComponentEndDate, r.contribution_id, 'DRAFT');
    END IF;


    IF r.status = 'EXCLUDED' AND previousComponentId > 0 THEN
      RAISE NOTICE 'Auditing EXCLUDED contribution => %, %', r.contribution_id, r.title;
      RAISE NOTICE 'Inserting DRAFT audit (creation, previous component end date) => %, %', r.creation, previousComponentStartDate;
      -- 7. Insert current status, not ended yet
      INSERT INTO public.contribution_status_audit(
        status_start_date, status_end_date, contribution_contribution_id, status)
      VALUES (r.creation, previousComponentStartDate, r.contribution_id, 'DRAFT');

      RAISE NOTICE 'Inserting PUBLISHED audit (creation, previous component end date) => %, %', previousComponentStartDate, previousComponentEndDate;
      -- 8. Insert current status, not ended yet
      INSERT INTO public.contribution_status_audit(
        status_start_date, status_end_date, contribution_contribution_id, status)
      VALUES (previousComponentStartDate, previousComponentEndDate, r.contribution_id, 'PUBLISHED');
    END IF;

  END LOOP;
  RETURN;
END
$BODY$
LANGUAGE plpgsql;

select create_contribution_audits();