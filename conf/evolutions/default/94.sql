# --- !Ups

CREATE OR REPLACE FUNCTION add_proposals_from_theme_to_wg
  (theme_id bigint, wg_id bigint, create_memberships boolean) RETURNS void AS $$
     DECLARE

       contribution contribution%ROWTYPE;;
       author appcivist_user%ROWTYPE;;
     BEGIN

       CREATE TEMPORARY TABLE temp_table  ON COMMIT DROP
         as select * from contribution
         where resource_space_resource_space_id
               in (
                 select distinct resource_space_resource_space_id
                 from resource_space_theme
                 where theme_theme_id = theme_id
          ) and type = 4 and
            NOT EXISTS ( select rs.working_group_group_id from
       resource_space_working_groups rs where rs.resource_space_resource_space_id = resource_space_resource_space_id
            and rs.working_group_group_id = wg_id);;

       insert into resource_space_working_groups
         select resource_space_resource_space_id, wg_id from temp_table;;

       IF create_memberships THEN
             FOR contribution IN SELECT * from temp_table
              LOOP
                for author in select * from contribution_appcivist_user where
                  contribution_contribution_id  = contribution.contribution_id and
                    NOT EXISTS (select * from membership where user_user_id = author.user_id
                    and working_group_group_id = wg_id)
                  LOOP
                      insert into membership(membership_type, status, creator_user_id, user_user_id, working_group_group_id)
                      VALUES ('GROUP', 'ACCEPTED', author.user_id, author.user_id, wg_id);;
                end loop;;
              END LOOP;;
       end if;;
     END;;
 $$ LANGUAGE plpgsql;

# --- !Downs
