CREATE TABLE notification_event_signal_user_archival
(
  id bigint NOT NULL,
  creation timestamp without time zone,
  last_update timestamp without time zone,
  lang character varying(255),
  removal timestamp without time zone,
  removed boolean,
  user_user_id bigint,
  signal_id bigint,
  read boolean,
  CONSTRAINT pk_notification_event_signal_archive_user PRIMARY KEY (id),
  CONSTRAINT fk_notification_event_signal_archive FOREIGN KEY (signal_id)
      REFERENCES notification_event_signal_archive (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_user FOREIGN KEY (user_user_id)
      REFERENCES appcivist_user (user_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE notification_event_signal_user
  OWNER TO postgres;
