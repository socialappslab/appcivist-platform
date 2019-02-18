# --- !Ups

CREATE TABLE contribution_status_audit
(
  id bigserial NOT NULL,
  status_start_date timestamp without time zone not null,
  status_end_date timestamp without time zone,
  contribution_contribution_id bigint NOT NULL,
  status character varying(25),
  CONSTRAINT pk_contribution_contribution_status_audit PRIMARY KEY (id),
  CONSTRAINT fk_contribution_status_audit_contribution FOREIGN KEY (contribution_contribution_id)
      REFERENCES contribution (contribution_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ck_contribution_status_audit_status CHECK (status::text = ANY (ARRAY['NEW'::character varying, 'PUBLISHED'::character varying,
  'ARCHIVED'::character varying, 'EXCLUDED'::character varying, 'DRAFT'::character varying, 'INBALLOT'::character varying]::text[]))
);

# --- !Downs
