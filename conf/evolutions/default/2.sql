# TODO: 
# 1. Add cascade rules to relationships (so that CASCADE rules are enforced even if different ORMs access the DB)
# 2. Add auditing for important tables
# --- !Ups

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

alter sequence ballot_papers_id_seq
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
    id                              bigitn NOT NULL,
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

create index ix_ballot_uuid on ballot_registration_field(uuid);

create index ix_candidate_uuid on candidate(uuid);

# --- !Downs

drop table if exists ballot_configuration cascade;

drop sequence if exists ballot_configurations_id_seq;

drop table if exists ballot_paper;

drop table if exists candidate; 

drop sequence if exists candidates_id_seq;

drop table if exists vote;

drop sequence if exists votes_id_seq;

drop index if exists ix_ballot_uuid;

drop index if exists ix_candidate_uuid;