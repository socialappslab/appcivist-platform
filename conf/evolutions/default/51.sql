# --- !Ups

-- 51. SQL
alter table subscription drop column user_user_id;
alter table subscription add column user_id character varying(40);

alter table subscription drop column space_id;
alter table subscription add column space_id character varying(40);

DROP TABLE public.notification_event_signal;


CREATE SEQUENCE public.notification_event_signal_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;




CREATE TABLE public.notification_event_signal
(
  id bigint NOT NULL DEFAULT nextval('notification_event_signal_id_seq'::regclass),
  creation timestamp without time zone,
  last_update timestamp without time zone,
  lang character varying(255),
  removal timestamp without time zone,
  removed boolean,
  space_type character varying(255),
  signal_type character varying(255),
  event_id character varying(40),
  text text,
  title character varying(255),
  data jsonb,
  CONSTRAINT pk_notification_event PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.notification_event_signal
  OWNER TO postgres;

-- Index: public.ix_notification_event_id

-- DROP INDEX public.ix_notification_event_id;

CREATE INDEX ix_notification_event_id
  ON public.notification_event_signal
  USING btree
  (id);

-- Index: public.ix_notification_event_uuid

# --- !Downs
