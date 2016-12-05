# --- !Ups
-- NEW for ContributionHistory support.
create table contribution_history (
  contribution_history_id   bigserial not null,
  contribution_id           bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  title                     varchar(255),
  text                      text,
  type                      integer,
  text_index                text,
  budget                    varchar(255),
  action_due_date           timestamp,
  action_done               boolean,
  action                    varchar(255),
  assessment_summary        varchar(255),
  constraint pk_contribution_history primary key (contribution_history_id))
;

create table contribution_history_appcivist_user (
  contribution_history_contribution_history_id   bigint not null,
  appcivist_user_user_id         bigint not null,
  constraint pk_contribution_history_appcivist_user primary key (contribution_history_contribution_history_id, appcivist_user_user_id))
;

create table resource_space_contribution_histories (
  resource_space_resource_space_id bigint not null,
  contribution_history_contribution_history_id   bigint not null,
  constraint pk_resource_space_contribution_histories primary key (resource_space_resource_space_id, contribution_history_contribution_history_id))
;

alter table contribution_history_appcivist_user add constraint fk_contribution_history_appcivist_use_01 foreign key (contribution_history_contribution_history_id) references contribution_history (contribution_history_id);
alter table contribution_history_appcivist_user add constraint fk_contribution_history_appcivist_use_02 foreign key (appcivist_user_user_id) references appcivist_user (user_id);

alter table resource_space_contribution_histories add constraint fk_resource_space_contribution_histories_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);
alter table resource_space_contribution_histories add constraint fk_resource_space_contribution_histories_02 foreign key (contribution_history_contribution_history_id) references contribution_history(contribution_history_id);

-- NEW for etherpad contributions template
ALTER TABLE resource ALTER COLUMN resource_type TYPE character varying (25);
ALTER TABLE resource DROP CONSTRAINT ck_resource_resource_type;
ALTER TABLE resource
  ADD CONSTRAINT ck_resource_resource_type CHECK (resource_type::text = ANY (ARRAY['PICTURE'::character varying, 'VIDEO'::character varying,
  'PAD'::character varying, 'TEXT'::character varying, 'WEBPAGE'::character varying, 'FILE'::character varying, 'AUDIO'::character varying,
  'CONTRIBUTION_TEMPLATE'::character varying, 'PROPOSAL'::character varying]::text[]));

ALTER TABLE resource ADD COLUMN confirmed boolean not null default false;

ALTER TABLE contribution add column status CHARACTER VARYING(15);
ALTER TABLE contribution
  ADD CONSTRAINT ck_contrinution_contrinbutoin_status CHECK (status::text = ANY (ARRAY['NEW'::character varying, 'PUBLISHED'::character varying,
  'ARCHIVED'::character varying, 'EXCLUDED'::character varying]::text[]));

create table non_member_author (
    id                              bigserial not null,
    name                            varchar(255),
    email                           varchar(255),
    url                             varchar(255),
    constraint pk_non_member_author primary key (id)

);

alter table contribution add column non_member_author_id bigint;
alter table contribution add constraint fk_non_member_author foreign key (non_member_author_id) references non_member_author(id);

ALTER TABLE contribution ADD COLUMN moderation_comment text;
ALTER TABLE contribution_history ADD COLUMN moderation_comment text;
ALTER TABLE resource ADD COLUMN title text;
ALTER TABLE resource ADD COLUMN description text;

-- NEW for etherpad campaign template
ALTER TABLE resource DROP CONSTRAINT ck_resource_resource_type;
ALTER TABLE resource
  ADD CONSTRAINT ck_resource_resource_type CHECK (resource_type::text = ANY (ARRAY['PICTURE'::character varying, 'VIDEO'::character varying,
  'PAD'::character varying, 'TEXT'::character varying, 'WEBPAGE'::character varying, 'FILE'::character varying, 'AUDIO'::character varying,
  'CONTRIBUTION_TEMPLATE'::character varying, 'CAMPAIGN_TEMPLATE'::character varying, 'PROPOSAL'::character varying]::text[]));


create table working_group_ballot_history(
  working_group_group_id bigint not null,
  ballot_id   bigint not null,
  constraint pk_working_group_ballot_history primary key (working_group_group_id, ballot_id))
;
alter table working_group_ballot_history add constraint fk_working_group_ballot_history_01 foreign key (working_group_group_id) references working_group (group_id);
alter table working_group_ballot_history add constraint fk_working_group_ballot_history_02 foreign key (ballot_id) references ballot(id);

alter table ballot add column status INTEGER;
alter table ballot add constraint "ck_ballot_status" check (status = ANY (ARRAY[0, 1]));
ALTER TABLE contribution ADD COLUMN source_code varchar(255);

ALTER TABLE contribution_feedback add column benefit INTEGER;
alter table contribution_feedback add constraint "ck_contribution_feedback_benefit" check (benefit = ANY (ARRAY[1, 2, 3, 4, 5]));
ALTER TABLE contribution_feedback add column need INTEGER;
alter table contribution_feedback add constraint "ck_contribution_feedback_need" check (need = ANY (ARRAY[1, 2, 3, 4, 5]));
ALTER TABLE contribution_feedback add column feasibility INTEGER;
alter table contribution_feedback add constraint "ck_contribution_feedback_feasibility" check (feasibility = ANY (ARRAY[1, 2, 3, 4, 5]));
ALTER TABLE contribution_feedback add column elegibility BOOLEAN;
ALTER TABLE contribution_feedback add column textual_feedback text;
alter table contribution_feedback add column type INTEGER;
alter table contribution_feedback add constraint "ck_contribution_feedback_type" check (type = ANY (ARRAY[0, 1, 2]));
alter table contribution_feedback add column status INTEGER;
alter table contribution_feedback add constraint "ck_contribution_feedback_status" check (type = ANY (ARRAY[0, 1]));
alter table contribution_feedback add column working_group_id BIGINT;
alter table contribution_feedback add column official_group_feedback BOOLEAN;
alter table contribution_feedback add column archived BOOLEAN;

# --- !Downs
drop table resource_space_contribution_histories;
drop table contribution_history_appcivist_user;
drop table contribution_history;
alter table contribution drop column non_member_author_id;
alter table contribution drop constraint fk_non_member_author;
drop table non_member_author;
drop table working_group_ballot_history;