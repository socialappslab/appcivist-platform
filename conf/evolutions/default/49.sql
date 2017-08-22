# --- !Ups
CREATE SEQUENCE public.subscription_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


CREATE TABLE public.subscription
(
  id bigint NOT NULL DEFAULT nextval('subscription_id_seq'::regclass),
  user_user_id bigint,
  space_id bigint,
  space_type varchar(255),
  subscription_type varchar(255),
  newsletter_frecuency integer,
  ignored_events jsonb,
  disabled_services jsonb,

  default_service integer,
  default_identity integer,

  CONSTRAINT pk_subscription PRIMARY KEY (id),
  CONSTRAINT fk_user FOREIGN KEY (user_user_id)
      REFERENCES public.appcivist_user (user_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

CREATE UNIQUE INDEX unique_subscription_of_user_in_space ON subscription (user_user_id, space_id, subscription_type);

# --- !Downs
DROP SEQUENCE public.subscription_id_seq;
DROP TABLE public.subscription;
DROP UNIQUE INDEX unique_subscription_of_user_in_space;
