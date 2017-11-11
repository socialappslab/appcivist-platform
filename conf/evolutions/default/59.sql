CREATE OR REPLACE FUNCTION move_signals(init_date timestamp) 
RETURNS void AS
$BODY$
begin
	case when (select count(*) FROM public.notification_event_signal where creation < init_date) > 0 then
		WITH tmp_event AS (DELETE FROM public.notification_event_signal where creation < init_date RETURNING *)
		INSERT INTO public.notification_event_signal_archive SELECT * FROM tmp_event;
	when (select count(*) FROM public.notification_event_signal_user where signal_id in 
	(SELECT id from public.notification_event_signal where creation < init_date ) > 0) THEN
		WITH tmp_user AS (DELETE FROM public.notification_event_signal_user where 
		signal_id in (SELECT id from public.notification_event_signal where creation < init_date )
		RETURNING *)
		INSERT INTO public.notification_event_signal_user_archive SELECT * FROM tmp_user;  
	end case;
	exception when others then
	   RAISE EXCEPTION 'ERROR. %', SQLERRM
	   USING ERRCODE = 'ER001';
end;
$BODY$
LANGUAGE plpgsql VOLATILE