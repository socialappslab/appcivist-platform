# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table assembly (
  assembly_id               bigint not null,
  name                      varchar(255),
  description               varchar(255),
  city                      varchar(255),
  icon                      varchar(255),
  url                       varchar(255),
  constraint pk_assembly primary key (assembly_id))
;

create table campaign (
  campaign_id               bigint not null,
  issue_issue_id            bigint not null,
  name                      varchar(255),
  url                       varchar(255),
  start_date                varchar(255),
  end_date                  varchar(255),
  campaignId                bigint,
  constraint pk_campaign primary key (campaign_id))
;

create table issue (
  issue_id                  bigint not null,
  assembly_assembly_id      bigint not null,
  title                     varchar(255),
  brief                     varchar(255),
  type                      varchar(255),
  resource_service_resource_id bigint,
  constraint pk_issue primary key (issue_id))
;

create table Linked_Account (
  account_id                bigint not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_Linked_Account primary key (account_id))
;

create table service (
  service_id                bigint not null,
  assembly_assembly_id      bigint not null,
  name                      varchar(255),
  base_url                  varchar(255),
  serviceAuthenticationId   bigint,
  constraint pk_service primary key (service_id))
;

create table service_auth (
  id                        bigint not null,
  auth_type                 varchar(255),
  token                     varchar(255),
  token_injection           varchar(255),
  token_param_name          varchar(255),
  constraint pk_service_auth primary key (id))
;

create table service_authentication (
  service_authentication_id bigint not null,
  auth_type                 varchar(255),
  token                     varchar(2048),
  token_injection           varchar(255),
  token_param_name          varchar(255),
  constraint pk_service_authentication primary key (service_authentication_id))
;

create table service_operation (
  service_operation_id      bigint not null,
  service_service_id        bigint not null,
  app_civist_operation      varchar(255),
  operationDefinitionId     bigint,
  constraint pk_service_operation primary key (service_operation_id))
;

create table service_operation_definition (
  operation_definition_id   bigint not null,
  name                      varchar(255),
  type                      varchar(255),
  method                    varchar(255),
  constraint pk_service_operation_definition primary key (operation_definition_id))
;

create table service_parameter (
  service_parameter_id      bigint not null,
  service_resource_service_resource_id bigint not null,
  value                     varchar(255),
  parameter_definition_id   bigint,
  constraint pk_service_parameter primary key (service_parameter_id))
;

create table service_parameter_definition (
  parameter_definition_id   bigint not null,
  service_operation_definition_operation_definition_id bigint not null,
  name                      varchar(255),
  type                      varchar(255),
  data_type                 varchar(255),
  constraint pk_service_parameter_definition primary key (parameter_definition_id))
;

create table service_resource (
  service_resource_id       bigint not null,
  service_service_id        bigint not null,
  url                       varchar(255),
  type                      varchar(255),
  key_value                 varchar(255),
  key_name                  varchar(255),
  constraint pk_service_resource primary key (service_resource_id))
;

create table Token_Action (
  token_id                  bigint not null,
  token                     varchar(255),
  user_id                   bigint,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_Token_Action_type check (type in ('PR','EV')),
  constraint uq_Token_Action_token unique (token),
  constraint pk_Token_Action primary key (token_id))
;

create table appcivist_user (
  user_id                   bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  username                  varchar(255),
  locale                    varchar(255),
  email_verified            boolean,
  username_verified         boolean,
  profile_pic               varchar(255),
  conf_type                 varchar(255),
  constraint pk_appcivist_user primary key (user_id))
;

create sequence assembly_seq;

create sequence campaign_seq;

create sequence issue_seq;

create sequence Linked_Account_seq;

create sequence service_seq;

create sequence service_auth_seq;

create sequence service_authentication_seq;

create sequence service_operation_seq;

create sequence service_operation_definition_seq;

create sequence service_parameter_seq;

create sequence service_parameter_definition_seq;

create sequence service_resource_seq;

create sequence Token_Action_seq;

create sequence appcivist_user_seq;

alter table campaign add constraint fk_campaign_issue_1 foreign key (issue_issue_id) references issue (issue_id);
create index ix_campaign_issue_1 on campaign (issue_issue_id);
alter table campaign add constraint fk_campaign_previousCampaign_2 foreign key (campaignId) references campaign (campaign_id);
create index ix_campaign_previousCampaign_2 on campaign (campaignId);
alter table issue add constraint fk_issue_assembly_3 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_issue_assembly_3 on issue (assembly_assembly_id);
alter table issue add constraint fk_issue_resource_4 foreign key (resource_service_resource_id) references service_resource (service_resource_id);
create index ix_issue_resource_4 on issue (resource_service_resource_id);
alter table Linked_Account add constraint fk_Linked_Account_user_5 foreign key (user_id) references appcivist_user (user_id);
create index ix_Linked_Account_user_5 on Linked_Account (user_id);
alter table service add constraint fk_service_assembly_6 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_service_assembly_6 on service (assembly_assembly_id);
alter table service add constraint fk_service_auth_7 foreign key (serviceAuthenticationId) references service_authentication (service_authentication_id);
create index ix_service_auth_7 on service (serviceAuthenticationId);
alter table service_operation add constraint fk_service_operation_service_8 foreign key (service_service_id) references service (service_id);
create index ix_service_operation_service_8 on service_operation (service_service_id);
alter table service_operation add constraint fk_service_operation_definitio_9 foreign key (operationDefinitionId) references service_operation_definition (operation_definition_id);
create index ix_service_operation_definitio_9 on service_operation (operationDefinitionId);
alter table service_parameter add constraint fk_service_parameter_service__10 foreign key (service_resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_parameter_service__10 on service_parameter (service_resource_service_resource_id);
alter table service_parameter add constraint fk_service_parameter_serviceP_11 foreign key (parameter_definition_id) references service_parameter_definition (parameter_definition_id);
create index ix_service_parameter_serviceP_11 on service_parameter (parameter_definition_id);
alter table service_parameter_definition add constraint fk_service_parameter_definiti_12 foreign key (service_operation_definition_operation_definition_id) references service_operation_definition (operation_definition_id);
create index ix_service_parameter_definiti_12 on service_parameter_definition (service_operation_definition_operation_definition_id);
alter table service_resource add constraint fk_service_resource_service_13 foreign key (service_service_id) references service (service_id);
create index ix_service_resource_service_13 on service_resource (service_service_id);
alter table Token_Action add constraint fk_Token_Action_targetUser_14 foreign key (user_id) references appcivist_user (user_id);
create index ix_Token_Action_targetUser_14 on Token_Action (user_id);



# --- !Downs

drop table if exists assembly cascade;

drop table if exists campaign cascade;

drop table if exists issue cascade;

drop table if exists Linked_Account cascade;

drop table if exists service cascade;

drop table if exists service_auth cascade;

drop table if exists service_authentication cascade;

drop table if exists service_operation cascade;

drop table if exists service_operation_definition cascade;

drop table if exists service_parameter cascade;

drop table if exists service_parameter_definition cascade;

drop table if exists service_resource cascade;

drop table if exists Token_Action cascade;

drop table if exists appcivist_user cascade;

drop sequence if exists assembly_seq;

drop sequence if exists campaign_seq;

drop sequence if exists issue_seq;

drop sequence if exists Linked_Account_seq;

drop sequence if exists service_seq;

drop sequence if exists service_auth_seq;

drop sequence if exists service_authentication_seq;

drop sequence if exists service_operation_seq;

drop sequence if exists service_operation_definition_seq;

drop sequence if exists service_parameter_seq;

drop sequence if exists service_parameter_definition_seq;

drop sequence if exists service_resource_seq;

drop sequence if exists Token_Action_seq;

drop sequence if exists appcivist_user_seq;

