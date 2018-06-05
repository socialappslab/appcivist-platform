CREATE OR REPLACE FUNCTION update_duplicate_themes()
  RETURNS void AS
$BODY$
DECLARE
    r theme%rowtype;
    oldest theme%rowtype;
BEGIN
    FOR r IN
        (select * from theme ou
	where (select count(*) from theme inr
	where inr.title = ou.title and inr.type = 'EMERGENT') > 1 AND type = 'EMERGENT' order by title, creation)
    LOOP
        IF oldest IS NULL or (r.title <> oldest.title) THEN
            oldest = r;
            raise notice 'A mantener: %', oldest.theme_id;
        else
	   raise notice 'A actualizar: %', r.theme_id;	
	   update resource_space_theme set theme_theme_id = oldest.theme_id where theme_theme_id = r.theme_id  and (select count(*) from theme where
		theme_id = oldest.theme_id ) = 0;
	   delete from resource_space_theme where theme_theme_id = r.theme_id;
	   delete from theme where theme_id = r.theme_id;

        end if;
        
    END LOOP;
    RETURN;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION update_duplicate_themes()
  OWNER TO postgres;

select * from update_duplicate_themes();
