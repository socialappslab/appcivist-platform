CREATE OR REPLACE FUNCTION add_proposals_from_theme_to_wg(theme_id bigint, wg_id bigint) RETURNS void AS $$
    DECLARE

    BEGIN
      insert into resource_space_working_groups
        select resource_space_resource_space_id, wg_id from contribution where resource_space_resource_space_id in (
        select distinct resource_space_resource_space_id
        from resource_space_theme
        where theme_theme_id = theme_id
      ) and type = 4;
    END;
$$ LANGUAGE plpgsql;
