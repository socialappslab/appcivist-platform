# --- !Ups

CREATE OR REPLACE FUNCTION add_proposals_from_theme_to_wg
  (theme_id bigint, wg_id bigint, create_memberships boolean) RETURNS void AS $$
DECLARE

  contribution contribution%ROWTYPE;;
  author appcivist_user%ROWTYPE;;

  numberContributions BIGINT;;
  membershipId BIGINT;;
  has_role BIGINT;;
  groupRsId BIGINT;;

BEGIN
  raise notice 'Creating temporary table for contributions in Theme = % ...', theme_id;;

  CREATE TEMPORARY TABLE temp_table  ON COMMIT DROP
    as select * from contribution
    where resource_space_resource_space_id
          in (
            select distinct resource_space_resource_space_id
            from resource_space_theme
            where theme_theme_id = theme_id
          ) and type = 4 and
          NOT EXISTS ( select wg.group_id from working_group wg, resource_space_contributions rs
          where wg.resources_resource_space_id = rs.resource_space_resource_space_id
                and rs.contribution_contribution_id = contribution_id
                and wg.group_id = wg_id);;

  SELECT count(*) FROM temp_table into numberContributions;;
  SELECT resources_resource_space_id from working_group where group_id = wg_id into groupRsId;;

  RAISE NOTICE 'Inserting % contributions INTO working GROUP % using RS % ...', numberContributions, wg_id, groupRsId;;

  insert into resource_space_contributions
    select groupRsId, contribution_id from temp_table;;

  IF create_memberships THEN
    FOR contribution IN SELECT * from temp_table
    LOOP
      for author in select appcivist_user_user_id as user_id, contribution_contribution_id from contribution_appcivist_user where
        contribution_contribution_id  = contribution.contribution_id and
        NOT EXISTS (select * from membership where user_user_id = author.user_id
                                                   and working_group_group_id = wg_id)
      LOOP

        select membership_id from membership
        where membership_type = 'GROUP'
              and creator_user_id = author.user_id
              and user_user_id = author.user_id
              and working_group_group_id = wg_id
        into membershipId;;

        IF membershipId > 0 THEN
          RAISE NOTICE 'User % already a member of group %',author.user_id, wg_id;;
        ELSE
          RAISE NOTICE 'Inserting membership for user % in GROUP % ...',author.user_id, wg_id;;
          insert into membership(membership_type, status, creator_user_id, user_user_id, working_group_group_id)
          VALUES ('GROUP', 'ACCEPTED', author.user_id, author.user_id, wg_id);;

          select membership_id from membership
          where membership_type = 'GROUP'
                and creator_user_id = author.user_id
                and user_user_id = author.user_id
                and working_group_group_id = wg_id
          into membershipId;;

          RAISE NOTICE 'New Membership ID  for user % is % ...',author.user_id, membershipId;;


          select membership_membership_id from membership_role where membership_membership_id = membershipId
          into has_role;;

          IF has_role > 0 THEN
            RAISE NOTICE 'Membership ID for user % already has role % ...',author.user_id, membershipId;;
          ELSE
            RAISE NOTICE 'Inserting role for user % in membership % ...',author.user_id, membershipId;;
            insert into membership_role values(membershipId, 4);;
          end if;;
        end if;;
      end loop;;
    END LOOP;;
  end if;;
END;;
$$ LANGUAGE plpgsql;

# --- !Downs
