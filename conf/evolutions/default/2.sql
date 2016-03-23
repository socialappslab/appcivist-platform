# TODO: 
# 1. Add cascade rules to relationships (so that CASCADE rules are enforced even if different ORMs access the DB)
# 2. Add auditing for important tables
# --- !Ups

create table ballot_registration_field (
  id                        bigint not null,
  ballot_id                 bigint,
  name                      varchar(255),
  description               text,
  expected_value            text,
  position                  integer,
  removed                   boolean,
  removed_at                timestamp,
  constraint pk_ballot_registration_field primary key (id))
;

create table ballot (
  id                        bigint not null,
  uuid                      varchar(40),
  password                  varchar(255),
  instructions              text,
  notes                     text,
  voting_system_type        varchar(11),
  starts_at                 timestamp,
  ends_at                   timestamp,
  created_at                timestamp,
  updated_at                timestamp,
  removed                   boolean,
  removed_at                timestamp,
  constraint ck_ballot_voting_system_type check (voting_system_type in ('RANGE','RANKED','DISTRIBUTED','PLURALITY','CONSENSUS')),
  constraint pk_ballot primary key (id))
;

create sequence ballot_registration_fields_id_seq;

create sequence ballots_id_seq;

create table resource_space_ballots (
  resource_space_resource_space_id bigint not null,
  ballot_id                      bigint not null,
  constraint pk_resource_space_ballots primary key (resource_space_resource_space_id, ballot_id))
;

alter table resource_space_ballots add constraint fk_resource_space_ballots_res_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_ballots add constraint fk_resource_space_ballots_bal_02 foreign key (ballot_id) references ballot (id);

create index ix_ballot_registration_field__49 on ballot_registration_field(id);

create index ix_ballot_id_50 on ballot(id);

create table ballot_configuration (
    id                        bigint NOT NULL,
    ballot_id                 bigint,
    key                       character varying,
    value                     character varying,
    "position"                integer,
    created_at                timestamp,
    updated_at                timestamp,
    removed_at                timestamp,
    removed                   boolean
);

create sequence ballot_configurations_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

alter sequence ballot_configurations_id_seq OWNED BY ballot_configuration.id;

create table ballot_paper (
    id                              bigint NOT NULL,
    ballot_id                       bigint,
    uuid                            character varying,
    signature                       character varying,
    status                          integer,
    created_at                      timestamp,
    updated_at                      timestamp, 
    removed_at                      timestamp,
    removed                         boolean
);

create sequence ballot_papers_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

alter sequence ballot_papers_id_seq OWNED BY ballot_paper.id;

create table candidate (
    id                              bigint NOT NULL,
    ballot_id                       bigint,
    uuid                            character varying,
    candidate_type                  integer,
    contribution_uuid               character varying,
    created_at                      timestamp,
    updated_at                      timestamp, 
    removed_at                      timestamp,
    removed                         boolean
);

create sequence candidates_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

alter sequence candidates_id_seq OWNED BY candidate.id;

create table vote (
    id                              bigint NOT NULL,
    candidate_id                    bigint,
    ballot_paper_id                 bigint,
    value                           character varying,
    value_type                      integer,
    created_at                      timestamp,
    updated_at                      timestamp,
    removed_at                      timestamp,
    removed                         boolean
);

create sequence votes_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

alter sequence votes_id_seq OWNED BY vote.id;

create index ix_candidate_uuid on candidate(uuid);

# --- !Downs

drop table if exists resource_space_ballots cascade;

drop table if exists ballot_registration_field cascade;

drop table if exists ballot cascade;

drop sequence if exists ballot_registration_fields_id_seq;

drop sequence if exists ballots_id_seq;

drop table if exists ballot_configuration cascade;

drop sequence if exists ballot_configurations_id_seq;

drop table if exists ballot_paper;

drop table if exists candidate; 

drop sequence if exists candidates_id_seq;

drop table if exists vote;

drop sequence if exists votes_id_seq;

drop index if exists ix_ballot_uuid;

drop index if exists ix_candidate_uuid;