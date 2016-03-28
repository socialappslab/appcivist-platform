# TODO: 
# 1. Add cascade rules to relationships (so that CASCADE rules are enforced even if different ORMs access the DB)
# 2. Add auditing for important tables
# --- !Ups

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

create sequence ballots_id_seq start with 9000;
alter sequence ballots_id_seq OWNED BY ballot.id;


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

create sequence ballot_registration_fields_id_seq start with 9000;
alter sequence ballot_registration_fields_id_seq OWNED BY ballot_registration_field.id;

alter table ballot_registration_field add constraint fk_registration_field_ballot foreign key (ballot_id) references ballot (id);

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
    removed                   boolean,
    constraint pk_ballot_configuration primary key (id)    
);

alter table ballot_configuration add constraint fk_ballot_config_ballot foreign key (ballot_id) references ballot (id);

create sequence ballot_configurations_id_seq
    START WITH 9000
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
    removed                         boolean,
    constraint pk_ballot_paper primary key (id)
);

alter table ballot_paper add constraint fk_ballot_paper_ballot foreign key (ballot_id) references ballot (id);

create sequence ballot_papers_id_seq
    START WITH 9000
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
    removed                         boolean,
    constraint pk_candidate primary key (id)    
);

alter table candidate add constraint fk_candidate_ballot foreign key (ballot_id) references ballot (id);


create sequence candidates_id_seq
    START WITH 9000
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
    removed                         boolean,
    constraint pk_vote primary key (id)    
);

alter table vote add constraint fk_vote_candidate foreign key (candidate_id) references candidate (id);
alter table vote add constraint fk_vote_ballot_paper foreign key (ballot_paper_id) references ballot_paper (id);


create sequence votes_id_seq
    START WITH 9000
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

alter sequence votes_id_seq OWNED BY vote.id;

create index ix_candidate_uuid on candidate(uuid);
create index ix_ballot_paper_signature on ballot_paper(signature);
create index ix_ballot_configuration_key on ballot_configuration(key);


# --- !Downs

drop table if exists resource_space_ballots cascade;
drop table if exists ballot_registration_field cascade;
drop table if exists ballot_configuration cascade;
drop table if exists vote;
drop table if exists candidate; 
drop table if exists ballot_paper;
drop table if exists ballot cascade;

drop sequence if exists ballot_registration_fields_id_seq;
drop sequence if exists ballot_configurations_id_seq;
drop sequence if exists votes_id_seq;
drop sequence if exists candidates_id_seq;
drop sequence if exists ballot_papers_id_seq;
drop sequence if exists ballots_id_seq;

drop index if exists ix_ballot_uuid;
drop index if exists ix_candidate_uuid;
drop index if exists ix_ballot_paper_signature;
drop index if exists ix_ballot_configuration_key;