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
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  location_location_id      bigint,
  constraint pk_assembly primary key (assembly_id))
;

create table campaign (
  campaign_id               bigint not null,
  name                      varchar(255),
  url                       varchar(255),
  start_date                varchar(255),
  end_date                  varchar(255),
  enabled                   boolean,
  test                      varchar(255),
  previous_campaign         bigint,
  next_campaign             bigint,
  issue_issue_id            bigint,
  start_operation_service_operation_id bigint,
  start_operation_type      varchar(255),
  constraint pk_campaign primary key (campaign_id))
;

create table config (
  config_id                 bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  key                       varchar(255),
  value                     varchar(255),
  module_mod_id             bigint,
  constraint pk_config primary key (config_id))
;

create table geo (
  location_id               bigint not null,
  type                      varchar(255),
  constraint pk_geo primary key (location_id))
;

create table geometry (
  geometry_id               bigint not null,
  type                      integer,
  coordinates               varchar(255),
  geo_location_id           bigint,
  constraint ck_geometry_type check (type in (0,1,2,3,4,5)),
  constraint pk_geometry primary key (geometry_id))
;

create table issue (
  issue_id                  bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  title                     varchar(255),
  brief                     varchar(255),
  type                      varchar(255),
  likes                     bigint,
  resource_service_resource_id bigint,
  assembly_assembly_id      bigint,
  location_location_id      bigint,
  constraint pk_issue primary key (issue_id))
;

create table Linked_Account (
  account_id                bigint not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_Linked_Account primary key (account_id))
;

create table meeting (
  meeting_id                bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  date                      timestamp,
  topic                     varchar(255),
  place                     varchar(255),
  status                    integer,
  doodle                    varchar(255),
  hangout                   varchar(255),
  constraint ck_meeting_status check (status in (0,1,2)),
  constraint pk_meeting primary key (meeting_id))
;

create table membership (
  MEMBERSHIP_TYPE           varchar(31) not null,
  membership_id             bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  expiration                timestamp,
  status                    integer,
  user_user_id              bigint,
  role_role_id              bigint,
  organization_org_id       bigint,
  working_group_group_id    bigint,
  constraint ck_membership_status check (status in (0,1,2,3)),
  constraint pk_membership primary key (membership_id))
;

create table message (
  message_id                bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  title                     varchar(255),
  text                      varchar(255),
  type                      integer,
  target_user_user_id       bigint,
  targe_working_group_group_id bigint,
  assembly_assembly_id      bigint,
  organization_org_id       bigint,
  constraint ck_message_type check (type in (0,1,2,3)),
  constraint pk_message primary key (message_id))
;

create table module (
  mod_id                    bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  enabled                   boolean,
  name                      varchar(255),
  constraint pk_module primary key (mod_id))
;

create table note (
  note_id                   bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  title                     varchar(255),
  text                      varchar(255),
  constraint pk_note primary key (note_id))
;

create table organization (
  org_id                    bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  name                      varchar(255),
  description               varchar(255),
  address                   varchar(255),
  location_location_id      bigint,
  constraint pk_organization primary key (org_id))
;

create table permission (
  permit_id                 bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  permit                    integer,
  constraint ck_permission_permit check (permit in (0,1,2,3)),
  constraint pk_permission primary key (permit_id))
;

create table phase (
  phase_id                  bigint not null,
  start_date                timestamp,
  end_date                  timestamp,
  update                    timestamp,
  name                      varchar(255),
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  constraint pk_phase primary key (phase_id))
;

create table profile (
  profile_id                bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  name                      varchar(255),
  middle_name               varchar(255),
  last_name                 varchar(255),
  birthdate                 timestamp,
  address                   varchar(255),
  constraint pk_profile primary key (profile_id))
;

create table properties (
  properties_id             bigint not null,
  key                       varchar(255),
  value                     varchar(255),
  geo_location_id           bigint,
  constraint pk_properties primary key (properties_id))
;

create table resource (
  resource_id               bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  type                      varchar(255),
  external_url              varchar(255),
  location_location_id      bigint,
  constraint pk_resource primary key (resource_id))
;

create table role (
  role_id                   bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  name                      varchar(255),
  constraint pk_role primary key (role_id))
;

create table service (
  service_id                bigint not null,
  name                      varchar(255),
  base_url                  varchar(255),
  assembly_assembly_id      bigint,
  service_definition_service_definition_id bigint,
  trailing_slash            boolean,
  constraint pk_service primary key (service_id))
;

create table service_authentication (
  service_authentication_id bigint not null,
  auth_type                 varchar(255),
  token                     varchar(2048),
  token_injection           varchar(255),
  token_param_name          varchar(255),
  service_service_id        bigint,
  constraint pk_service_authentication primary key (service_authentication_id))
;

create table service_definition (
  service_definition_id     bigint not null,
  name                      varchar(255),
  constraint pk_service_definition primary key (service_definition_id))
;

create table service_operation (
  service_operation_id      bigint not null,
  app_civist_operation      varchar(255),
  expected_resource         varchar(255),
  operation_definition_id   bigint,
  service_service_id        bigint,
  constraint pk_service_operation primary key (service_operation_id))
;

create table service_operation_definition (
  operation_definition_id   bigint not null,
  name                      varchar(255),
  type                      varchar(255),
  method                    varchar(255),
  service_definition_service_definition_id bigint,
  name_on_path              boolean,
  constraint pk_service_operation_definition primary key (operation_definition_id))
;

create table service_parameter (
  service_parameter_id      bigint not null,
  value                     varchar(255),
  service_parameter_parameter_definition_id bigint,
  service_resource_service_resource_id bigint,
  service_operation_service_operation_id bigint,
  constraint pk_service_parameter primary key (service_parameter_id))
;

create table service_parameter_data_model (
  data_model_id             bigint not null,
  data_key                  varchar(255),
  annotations               varchar(255),
  default_value             varchar(255),
  required                  boolean,
  list                      boolean,
  definition_parameter_definition_id bigint,
  parent_data_model_data_model_id bigint,
  constraint pk_service_parameter_data_model primary key (data_model_id))
;

create table service_parameter_definition (
  parameter_definition_id   bigint not null,
  service_operation_definition_operation_definition_id bigint not null,
  name                      varchar(255),
  type                      varchar(255),
  data_type                 varchar(255),
  path_order                integer,
  default_value             varchar(255),
  required                  boolean,
  constraint pk_service_parameter_definition primary key (parameter_definition_id))
;

create table service_resource (
  service_resource_id       bigint not null,
  url                       varchar(255),
  type                      varchar(255),
  key_value                 varchar(255),
  key_name                  varchar(255),
  body                      varchar(255),
  service_service_id        bigint,
  parent_resource_service_resource_id bigint,
  constraint pk_service_resource primary key (service_resource_id))
;

create table task (
  task_id                   bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  title                     varchar(255),
  description               varchar(255),
  due_date                  timestamp,
  status                    integer,
  places                    varchar(255),
  constraint ck_task_status check (status in (0,1,2)),
  constraint pk_task primary key (task_id))
;

create table theme (
  theme_id                  bigint not null,
  title                     varchar(255),
  description               varchar(255),
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  constraint pk_theme primary key (theme_id))
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
  role_role_id              bigint,
  constraint pk_appcivist_user primary key (user_id))
;

create table working_group (
  group_id                  bigint not null,
  creation                  timestamp,
  removal                   timestamp,
  lang                      varchar(255),
  name                      varchar(255),
  text                      varchar(255),
  expiration                timestamp,
  is_public                 boolean,
  accept_requests           boolean,
  role_role_id              bigint,
  constraint pk_working_group primary key (group_id))
;


create table assembly_theme (
  assembly_assembly_id           bigint not null,
  theme_theme_id                 bigint not null,
  constraint pk_assembly_theme primary key (assembly_assembly_id, theme_theme_id))
;

create table assembly_phase (
  assembly_assembly_id           bigint not null,
  phase_phase_id                 bigint not null,
  constraint pk_assembly_phase primary key (assembly_assembly_id, phase_phase_id))
;

create table assembly_module (
  assembly_assembly_id           bigint not null,
  module_mod_id                  bigint not null,
  constraint pk_assembly_module primary key (assembly_assembly_id, module_mod_id))
;

create table assembly_organization (
  assembly_assembly_id           bigint not null,
  organization_org_id            bigint not null,
  constraint pk_assembly_organization primary key (assembly_assembly_id, organization_org_id))
;

create table assembly_message (
  assembly_assembly_id           bigint not null,
  message_message_id             bigint not null,
  constraint pk_assembly_message primary key (assembly_assembly_id, message_message_id))
;

create table assembly_working_group (
  assembly_assembly_id           bigint not null,
  working_group_group_id         bigint not null,
  constraint pk_assembly_working_group primary key (assembly_assembly_id, working_group_group_id))
;

create table campaign_service_operation (
  campaign_campaign_id           bigint not null,
  service_operation_service_operation_id bigint not null,
  constraint pk_campaign_service_operation primary key (campaign_campaign_id, service_operation_service_operation_id))
;

create table campaign_service_resource (
  campaign_campaign_id           bigint not null,
  service_resource_service_resource_id bigint not null,
  constraint pk_campaign_service_resource primary key (campaign_campaign_id, service_resource_service_resource_id))
;

create table issue_theme (
  issue_issue_id                 bigint not null,
  theme_theme_id                 bigint not null,
  constraint pk_issue_theme primary key (issue_issue_id, theme_theme_id))
;

create table issue_resource (
  issue_issue_id                 bigint not null,
  resource_resource_id           bigint not null,
  constraint pk_issue_resource primary key (issue_issue_id, resource_resource_id))
;

create table organization_theme (
  organization_org_id            bigint not null,
  theme_theme_id                 bigint not null,
  constraint pk_organization_theme primary key (organization_org_id, theme_theme_id))
;

create table organization_message (
  organization_org_id            bigint not null,
  message_message_id             bigint not null,
  constraint pk_organization_message primary key (organization_org_id, message_message_id))
;

create table phase_module (
  phase_phase_id                 bigint not null,
  module_mod_id                  bigint not null,
  constraint pk_phase_module primary key (phase_phase_id, module_mod_id))
;

create table phase_resource (
  phase_phase_id                 bigint not null,
  resource_resource_id           bigint not null,
  constraint pk_phase_resource primary key (phase_phase_id, resource_resource_id))
;

create table RELATED_RESOURCES (
  source                         bigint not null,
  target                         bigint not null,
  constraint pk_RELATED_RESOURCES primary key (source, target))
;

create table working_group_resource (
  working_group_group_id         bigint not null,
  resource_resource_id           bigint not null,
  constraint pk_working_group_resource primary key (working_group_group_id, resource_resource_id))
;
create sequence assembly_seq;

create sequence campaign_seq;

create sequence config_seq;

create sequence geo_seq;

create sequence geometry_seq;

create sequence issue_seq;

create sequence Linked_Account_seq;

create sequence meeting_seq;

create sequence membership_seq;

create sequence message_seq;

create sequence module_seq;

create sequence note_seq;

create sequence organization_seq;

create sequence permission_seq;

create sequence phase_seq;

create sequence profile_seq;

create sequence properties_seq;

create sequence resource_seq;

create sequence role_seq;

create sequence service_seq;

create sequence service_authentication_seq;

create sequence service_definition_seq;

create sequence service_operation_seq;

create sequence service_operation_definition_seq;

create sequence service_parameter_seq;

create sequence service_parameter_data_model_seq;

create sequence service_parameter_definition_seq;

create sequence service_resource_seq;

create sequence task_seq;

create sequence theme_seq;

create sequence Token_Action_seq;

create sequence appcivist_user_seq;

create sequence working_group_seq;

alter table assembly add constraint fk_assembly_location_1 foreign key (location_location_id) references geo (location_id);
create index ix_assembly_location_1 on assembly (location_location_id);
alter table campaign add constraint fk_campaign_previousCampaign_2 foreign key (previous_campaign) references campaign (campaign_id);
create index ix_campaign_previousCampaign_2 on campaign (previous_campaign);
alter table campaign add constraint fk_campaign_nextCampaign_3 foreign key (next_campaign) references campaign (campaign_id);
create index ix_campaign_nextCampaign_3 on campaign (next_campaign);
alter table campaign add constraint fk_campaign_issue_4 foreign key (issue_issue_id) references issue (issue_id);
create index ix_campaign_issue_4 on campaign (issue_issue_id);
alter table campaign add constraint fk_campaign_startOperation_5 foreign key (start_operation_service_operation_id) references service_operation (service_operation_id);
create index ix_campaign_startOperation_5 on campaign (start_operation_service_operation_id);
alter table config add constraint fk_config_module_6 foreign key (module_mod_id) references module (mod_id);
create index ix_config_module_6 on config (module_mod_id);
alter table geometry add constraint fk_geometry_geo_7 foreign key (geo_location_id) references geo (location_id);
create index ix_geometry_geo_7 on geometry (geo_location_id);
alter table issue add constraint fk_issue_resource_8 foreign key (resource_service_resource_id) references service_resource (service_resource_id);
create index ix_issue_resource_8 on issue (resource_service_resource_id);
alter table issue add constraint fk_issue_assembly_9 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_issue_assembly_9 on issue (assembly_assembly_id);
alter table issue add constraint fk_issue_location_10 foreign key (location_location_id) references geo (location_id);
create index ix_issue_location_10 on issue (location_location_id);
alter table Linked_Account add constraint fk_Linked_Account_user_11 foreign key (user_id) references appcivist_user (user_id);
create index ix_Linked_Account_user_11 on Linked_Account (user_id);
alter table membership add constraint fk_membership_user_12 foreign key (user_user_id) references appcivist_user (user_id);
create index ix_membership_user_12 on membership (user_user_id);
alter table membership add constraint fk_membership_role_13 foreign key (role_role_id) references role (role_id);
create index ix_membership_role_13 on membership (role_role_id);
alter table membership add constraint fk_membership_organization_14 foreign key (organization_org_id) references organization (org_id);
create index ix_membership_organization_14 on membership (organization_org_id);
alter table membership add constraint fk_membership_workingGroup_15 foreign key (working_group_group_id) references working_group (group_id);
create index ix_membership_workingGroup_15 on membership (working_group_group_id);
alter table message add constraint fk_message_targetUser_16 foreign key (target_user_user_id) references appcivist_user (user_id);
create index ix_message_targetUser_16 on message (target_user_user_id);
alter table message add constraint fk_message_targeWorkingGroup_17 foreign key (targe_working_group_group_id) references working_group (group_id);
create index ix_message_targeWorkingGroup_17 on message (targe_working_group_group_id);
alter table message add constraint fk_message_assembly_18 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_message_assembly_18 on message (assembly_assembly_id);
alter table message add constraint fk_message_organization_19 foreign key (organization_org_id) references organization (org_id);
create index ix_message_organization_19 on message (organization_org_id);
alter table organization add constraint fk_organization_location_20 foreign key (location_location_id) references geo (location_id);
create index ix_organization_location_20 on organization (location_location_id);
alter table properties add constraint fk_properties_geo_21 foreign key (geo_location_id) references geo (location_id);
create index ix_properties_geo_21 on properties (geo_location_id);
alter table resource add constraint fk_resource_location_22 foreign key (location_location_id) references geo (location_id);
create index ix_resource_location_22 on resource (location_location_id);
alter table service add constraint fk_service_assembly_23 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_service_assembly_23 on service (assembly_assembly_id);
alter table service add constraint fk_service_serviceDefinition_24 foreign key (service_definition_service_definition_id) references service_definition (service_definition_id);
create index ix_service_serviceDefinition_24 on service (service_definition_service_definition_id);
alter table service_authentication add constraint fk_service_authentication_ser_25 foreign key (service_service_id) references service (service_id);
create index ix_service_authentication_ser_25 on service_authentication (service_service_id);
alter table service_operation add constraint fk_service_operation_definiti_26 foreign key (operation_definition_id) references service_operation_definition (operation_definition_id);
create index ix_service_operation_definiti_26 on service_operation (operation_definition_id);
alter table service_operation add constraint fk_service_operation_service_27 foreign key (service_service_id) references service (service_id);
create index ix_service_operation_service_27 on service_operation (service_service_id);
alter table service_operation_definition add constraint fk_service_operation_definiti_28 foreign key (service_definition_service_definition_id) references service_definition (service_definition_id);
create index ix_service_operation_definiti_28 on service_operation_definition (service_definition_service_definition_id);
alter table service_parameter add constraint fk_service_parameter_serviceP_29 foreign key (service_parameter_parameter_definition_id) references service_parameter_definition (parameter_definition_id);
create index ix_service_parameter_serviceP_29 on service_parameter (service_parameter_parameter_definition_id);
alter table service_parameter add constraint fk_service_parameter_serviceR_30 foreign key (service_resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_parameter_serviceR_30 on service_parameter (service_resource_service_resource_id);
alter table service_parameter add constraint fk_service_parameter_serviceO_31 foreign key (service_operation_service_operation_id) references service_operation (service_operation_id);
create index ix_service_parameter_serviceO_31 on service_parameter (service_operation_service_operation_id);
alter table service_parameter_data_model add constraint fk_service_parameter_data_mod_32 foreign key (definition_parameter_definition_id) references service_parameter_definition (parameter_definition_id);
create index ix_service_parameter_data_mod_32 on service_parameter_data_model (definition_parameter_definition_id);
alter table service_parameter_data_model add constraint fk_service_parameter_data_mod_33 foreign key (parent_data_model_data_model_id) references service_parameter_data_model (data_model_id);
create index ix_service_parameter_data_mod_33 on service_parameter_data_model (parent_data_model_data_model_id);
alter table service_parameter_definition add constraint fk_service_parameter_definiti_34 foreign key (service_operation_definition_operation_definition_id) references service_operation_definition (operation_definition_id);
create index ix_service_parameter_definiti_34 on service_parameter_definition (service_operation_definition_operation_definition_id);
alter table service_resource add constraint fk_service_resource_service_35 foreign key (service_service_id) references service (service_id);
create index ix_service_resource_service_35 on service_resource (service_service_id);
alter table service_resource add constraint fk_service_resource_parentRes_36 foreign key (parent_resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_resource_parentRes_36 on service_resource (parent_resource_service_resource_id);
alter table Token_Action add constraint fk_Token_Action_targetUser_37 foreign key (user_id) references appcivist_user (user_id);
create index ix_Token_Action_targetUser_37 on Token_Action (user_id);
alter table appcivist_user add constraint fk_appcivist_user_role_38 foreign key (role_role_id) references role (role_id);
create index ix_appcivist_user_role_38 on appcivist_user (role_role_id);
alter table working_group add constraint fk_working_group_role_39 foreign key (role_role_id) references role (role_id);
create index ix_working_group_role_39 on working_group (role_role_id);



alter table assembly_theme add constraint fk_assembly_theme_assembly_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_theme add constraint fk_assembly_theme_theme_02 foreign key (theme_theme_id) references theme (theme_id);

alter table assembly_phase add constraint fk_assembly_phase_assembly_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_phase add constraint fk_assembly_phase_phase_02 foreign key (phase_phase_id) references phase (phase_id);

alter table assembly_module add constraint fk_assembly_module_assembly_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_module add constraint fk_assembly_module_module_02 foreign key (module_mod_id) references module (mod_id);

alter table assembly_organization add constraint fk_assembly_organization_asse_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_organization add constraint fk_assembly_organization_orga_02 foreign key (organization_org_id) references organization (org_id);

alter table assembly_message add constraint fk_assembly_message_assembly_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_message add constraint fk_assembly_message_message_02 foreign key (message_message_id) references message (message_id);

alter table assembly_working_group add constraint fk_assembly_working_group_ass_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_working_group add constraint fk_assembly_working_group_wor_02 foreign key (working_group_group_id) references working_group (group_id);

alter table campaign_service_operation add constraint fk_campaign_service_operation_01 foreign key (campaign_campaign_id) references campaign (campaign_id);

alter table campaign_service_operation add constraint fk_campaign_service_operation_02 foreign key (service_operation_service_operation_id) references service_operation (service_operation_id);

alter table campaign_service_resource add constraint fk_campaign_service_resource__01 foreign key (campaign_campaign_id) references campaign (campaign_id);

alter table campaign_service_resource add constraint fk_campaign_service_resource__02 foreign key (service_resource_service_resource_id) references service_resource (service_resource_id);

alter table issue_theme add constraint fk_issue_theme_issue_01 foreign key (issue_issue_id) references issue (issue_id);

alter table issue_theme add constraint fk_issue_theme_theme_02 foreign key (theme_theme_id) references theme (theme_id);

alter table issue_resource add constraint fk_issue_resource_issue_01 foreign key (issue_issue_id) references issue (issue_id);

alter table issue_resource add constraint fk_issue_resource_resource_02 foreign key (resource_resource_id) references resource (resource_id);

alter table organization_theme add constraint fk_organization_theme_organiz_01 foreign key (organization_org_id) references organization (org_id);

alter table organization_theme add constraint fk_organization_theme_theme_02 foreign key (theme_theme_id) references theme (theme_id);

alter table organization_message add constraint fk_organization_message_organ_01 foreign key (organization_org_id) references organization (org_id);

alter table organization_message add constraint fk_organization_message_messa_02 foreign key (message_message_id) references message (message_id);

alter table phase_module add constraint fk_phase_module_phase_01 foreign key (phase_phase_id) references phase (phase_id);

alter table phase_module add constraint fk_phase_module_module_02 foreign key (module_mod_id) references module (mod_id);

alter table phase_resource add constraint fk_phase_resource_phase_01 foreign key (phase_phase_id) references phase (phase_id);

alter table phase_resource add constraint fk_phase_resource_resource_02 foreign key (resource_resource_id) references resource (resource_id);

alter table RELATED_RESOURCES add constraint fk_RELATED_RESOURCES_resource_01 foreign key (source) references resource (resource_id);

alter table RELATED_RESOURCES add constraint fk_RELATED_RESOURCES_resource_02 foreign key (target) references resource (resource_id);

alter table working_group_resource add constraint fk_working_group_resource_wor_01 foreign key (working_group_group_id) references working_group (group_id);

alter table working_group_resource add constraint fk_working_group_resource_res_02 foreign key (resource_resource_id) references resource (resource_id);

# --- !Downs

drop table if exists assembly cascade;

drop table if exists assembly_theme cascade;

drop table if exists assembly_phase cascade;

drop table if exists assembly_module cascade;

drop table if exists assembly_organization cascade;

drop table if exists assembly_message cascade;

drop table if exists assembly_working_group cascade;

drop table if exists campaign cascade;

drop table if exists campaign_service_operation cascade;

drop table if exists campaign_service_resource cascade;

drop table if exists config cascade;

drop table if exists geo cascade;

drop table if exists geometry cascade;

drop table if exists issue cascade;

drop table if exists issue_theme cascade;

drop table if exists issue_resource cascade;

drop table if exists Linked_Account cascade;

drop table if exists meeting cascade;

drop table if exists membership cascade;

drop table if exists message cascade;

drop table if exists module cascade;

drop table if exists phase_module cascade;

drop table if exists note cascade;

drop table if exists organization cascade;

drop table if exists organization_theme cascade;

drop table if exists organization_message cascade;

drop table if exists permission cascade;

drop table if exists phase cascade;

drop table if exists phase_resource cascade;

drop table if exists profile cascade;

drop table if exists properties cascade;

drop table if exists resource cascade;

drop table if exists RELATED_RESOURCES cascade;

drop table if exists working_group_resource cascade;

drop table if exists role cascade;

drop table if exists service cascade;

drop table if exists service_authentication cascade;

drop table if exists service_definition cascade;

drop table if exists service_operation cascade;

drop table if exists service_operation_definition cascade;

drop table if exists service_parameter cascade;

drop table if exists service_parameter_data_model cascade;

drop table if exists service_parameter_definition cascade;

drop table if exists service_resource cascade;

drop table if exists task cascade;

drop table if exists theme cascade;

drop table if exists Token_Action cascade;

drop table if exists appcivist_user cascade;

drop table if exists working_group cascade;

drop sequence if exists assembly_seq;

drop sequence if exists campaign_seq;

drop sequence if exists config_seq;

drop sequence if exists geo_seq;

drop sequence if exists geometry_seq;

drop sequence if exists issue_seq;

drop sequence if exists Linked_Account_seq;

drop sequence if exists meeting_seq;

drop sequence if exists membership_seq;

drop sequence if exists message_seq;

drop sequence if exists module_seq;

drop sequence if exists note_seq;

drop sequence if exists organization_seq;

drop sequence if exists permission_seq;

drop sequence if exists phase_seq;

drop sequence if exists profile_seq;

drop sequence if exists properties_seq;

drop sequence if exists resource_seq;

drop sequence if exists role_seq;

drop sequence if exists service_seq;

drop sequence if exists service_authentication_seq;

drop sequence if exists service_definition_seq;

drop sequence if exists service_operation_seq;

drop sequence if exists service_operation_definition_seq;

drop sequence if exists service_parameter_seq;

drop sequence if exists service_parameter_data_model_seq;

drop sequence if exists service_parameter_definition_seq;

drop sequence if exists service_resource_seq;

drop sequence if exists task_seq;

drop sequence if exists theme_seq;

drop sequence if exists Token_Action_seq;

drop sequence if exists appcivist_user_seq;

drop sequence if exists working_group_seq;

