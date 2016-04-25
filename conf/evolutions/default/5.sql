# --- !Ups
create table log (
    id                              bigint NOT NULL,
    time                            timestamp,
    user_id                         character varying,
    path                            character varying,
    action                          character varying,
    resource_type                   character varying,
    resource_uuid                   character varying,
    constraint pk_log primary key (id)
);

create sequence log_id_seq
    START WITH 9000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

alter sequence log_id_seq OWNED BY ballot_paper.id;
alter table log alter column id set default nextval('log_id_seq');
    
# --- !Downs
drop table if exists log cascade;
drop sequence if exists log_id_seq cascade;
