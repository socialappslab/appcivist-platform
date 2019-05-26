# --- !Ups

insert into  subscription
    (space_type, subscription_type, user_id, space_id)
    (
        select distinct 'CONTRIBUTION',
                        'REGULAR',
                        au.uuid             as user_id,
                        resource_space.uuid as resource_space_id
        from contribution
                 join resource_space on contribution.resource_space_resource_space_id = resource_space.resource_space_id
                 join resource_space_working_groups rswg
                      on contribution.resource_space_resource_space_id = rswg.resource_space_resource_space_id
                 join membership on rswg.working_group_group_id = membership.working_group_group_id
                 join appcivist_user au on membership.user_user_id = au.user_id
        where (contribution.status = 'PUBLISHED'
            or contribution.status = 'PUBLIC_DRAFT')
          and contribution.removed is false
    )

# --- !Downs