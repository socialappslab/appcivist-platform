ALTER TABLE contribution ADD column creator_user_id bigint;
ALTER TABLE contribution ADD constraint fk_contribution_user foreign key (creator_user_id) references appcivist_user (user_id);
UPDATE contribution set creator_user_id = (select appcivist_user_user_id from contribution_appcivist_user where contribution_contribution_id = contribution_id limit 1);
