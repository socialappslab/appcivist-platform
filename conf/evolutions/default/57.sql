CREATE TABLE notification_event_signal_archive
(
  id bigint NOT NULL,
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
  CONSTRAINT pk_notification_event_archive PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE notification_event_signal
  OWNER TO postgres;