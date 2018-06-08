CREATE OR REPLACE FUNCTION update_duplicate_users()
  RETURNS void AS
$BODY$
DECLARE
    r appcivist_user%rowtype;
    oldest appcivist_user%rowtype;
BEGIN
    FOR r IN
        (select * from appcivist_user ou
	join linked_account la on la.user_id = ou.user_id 
	where (select count(*) from appcivist_user inr
	where inr.email = ou.email) > 1 order by email, ou.user_id)
    LOOP
        IF oldest IS NULL or (r.email <> oldest.email) THEN
            oldest = r;
            raise notice 'A mantener: %', oldest.user_id;
        else
	   raise notice 'A actualizar: %', r.user_id;	
	   update linked_account set user_id = oldest.user_id where user_id = r.user_id;
	   update membership set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update contribution set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update contribution_appcivist_user set appcivist_user_user_id = oldest.user_id 
		where appcivist_user_user_id = r.user_id and (select count(*) from contribution_appcivist_user where
		appcivist_user_user_id = oldest.user_id ) = 0;
	   update assembly set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update campaign set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update campaign_participation set user_user_id = oldest.user_id where user_user_id = r.user_id;
	   update membership_invitation set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update notification_event_signal_user set user_user_id = oldest.user_id where user_user_id = r.user_id;
	   update subscription set user_id = oldest.uuid where user_id = r.uuid;
	   update working_group set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update appcivist_user set removed = true, email = r.email || '.invalid' where user_id = r.user_id;
        end if;
        
    END LOOP;
    RETURN;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION update_duplicate_users()
  OWNER TO postgres;

select * from update_duplicate_users();
