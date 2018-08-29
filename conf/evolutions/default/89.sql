
CREATE OR REPLACE FUNCTION change_status_contribution_in_campaign(search_shortname character, date timestamp, target character) RETURNS void AS $$
    BEGIN
      update contribution set status = target where contribution_id in (
      select contribution_id from contribution where contribution_id in (
      select contribution_contribution_id from resource_space_contributions where resource_space_resource_space_id in (
      SELECT c.resources_resource_space_id FROM campaign c WHERE c.shortname = search_shortname) and creation < date::timestamp));
    END;
    $$ LANGUAGE plpgsql;
