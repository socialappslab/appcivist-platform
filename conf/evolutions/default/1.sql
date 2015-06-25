# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table assembly (
  assembly_id               bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name                      varchar(255),
  description               varchar(255),
  city                      varchar(255),
  state                     varchar(255),
  country                   varchar(255),
  icon                      varchar(255),
  url                       varchar(255),
  visibiliy                 integer,
  membership_role           integer,
  location_location_id      bigint,
  constraint ck_assembly_visibiliy check (visibiliy in (0,1,2)),
  constraint ck_assembly_membership_role check (membership_role in (0,1,2,3)),
  constraint pk_assembly primary key (assembly_id))
;

create table assembly_connection (
  assembly_connection_id    bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  type                      integer,
  source_assembly_assembly_id bigint,
  target_assembly_assembly_id bigint,
  constraint ck_assembly_connection_type check (type in (0,1,2,3)),
  constraint pk_assembly_connection primary key (assembly_connection_id))
;

create table campaign (
  campaign_id               bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  start_date                timestamp,
  end_date                  timestamp,
  active                    boolean,
  url                       varchar(255),
  visibility                integer,
  assembly_assembly_id      bigint,
  type_campaign_type_id     bigint,
  constraint ck_campaign_visibility check (visibility in (0,1,2)),
  constraint pk_campaign primary key (campaign_id))
;

create table campaign_phase (
  phase_id                  bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  start_date                timestamp,
  end_date                  timestamp,
  campaign_campaign_id      bigint,
  definition_phase_definition_id bigint,
  can_overlap               boolean,
  constraint pk_campaign_phase primary key (phase_id))
;

create table campaign_phase_contribution (
  campaign_phase_contribution_id bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  type                      integer,
  contribution_contribution_id bigint,
  phase_phase_id            bigint,
  group_id                  bigint,
  constraint ck_campaign_phase_contribution_type check (type in (0,1,2)),
  constraint pk_campaign_phase_contribution primary key (campaign_phase_contribution_id))
;

create table campaign_type (
  campaign_type_id          bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name_key                  integer,
  name                      varchar(255),
  constraint ck_campaign_type_name_key check (name_key in (0,1,2,3,4)),
  constraint pk_campaign_type primary key (campaign_type_id))
;

create table category (
  category_id               bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  description               varchar(255),
  constraint pk_category primary key (category_id))
;

create table config (
  config_id                 bigint not null,
  module_mod_id             bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  key                       varchar(255),
  value                     varchar(255),
  config_target             integer,
  definition_config_definition_id bigint,
  campaign_campaign_id      bigint,
  campaign_phase_phase_id   bigint,
  assembly_assembly_id      bigint,
  working_group_group_id    bigint,
  constraint ck_config_config_target check (config_target in (0,1,2,3,4)),
  constraint pk_config primary key (config_id))
;

create table config_definition (
  config_definition_id      bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  key                       varchar(255),
  value_type                varchar(255),
  description               varchar(255),
  category                  varchar(255),
  constraint pk_config_definition primary key (config_definition_id))
;

create table contribution (
  contribution_id           bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  text                      varchar(255),
  type                      integer,
  assembly_assembly_id      bigint,
  location_location_id      bigint,
  stats_contribution_statistics_id bigint,
  constraint ck_contribution_type check (type in (0,1,2,3,4,5)),
  constraint pk_contribution primary key (contribution_id))
;

create table contribution_connection (
  contribution_connection_id bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  type                      integer,
  status                    integer,
  source_contribution_contribution_id bigint,
  target_contribution_contribution_id bigint,
  constraint ck_contribution_connection_type check (type in (0,1,2,3,4,5)),
  constraint ck_contribution_connection_status check (status in (0,1,2,3)),
  constraint pk_contribution_connection primary key (contribution_connection_id))
;

create table contribution_statistics (
  contribution_statistics_id bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  ups                       bigint,
  downs                     bigint,
  favs                      bigint,
  views                     bigint,
  replies                   bigint,
  flags                     bigint,
  constraint pk_contribution_statistics primary key (contribution_statistics_id))
;

create table geo (
  location_id               bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
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

create table hashtag (
  hashtag_id                bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  hashtag                   varchar(255),
  constraint pk_hashtag primary key (hashtag_id))
;

create table initial_data_config (
  data_file_id              bigint not null,
  data_file                 varchar(255),
  loaded                    boolean,
  constraint pk_initial_data_config primary key (data_file_id))
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
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  meeting_date              timestamp,
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
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  expiration                bigint,
  status                    integer,
  creator_user_id           bigint,
  user_user_id              bigint,
  assembly_assembly_id      bigint,
  working_group_group_id    bigint,
  constraint ck_membership_status check (status in (0,1,2,3)),
  constraint pk_membership primary key (membership_id))
;

create table message (
  message_id                bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  text                      varchar(255),
  type                      integer,
  target_user_user_id       bigint,
  target_working_group_group_id bigint,
  target_assembly_assembly_id bigint,
  constraint ck_message_type check (type in (0,1,2,3)),
  constraint pk_message primary key (message_id))
;

create table module (
  mod_id                    bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  enabled                   boolean,
  name                      varchar(255),
  constraint pk_module primary key (mod_id))
;

create table note (
  note_id                   bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  text                      varchar(255),
  constraint pk_note primary key (note_id))
;

create table phase_definition (
  phase_definition_id       bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name                      varchar(255),
  constraint pk_phase_definition primary key (phase_definition_id))
;

create table properties (
  properties_id             bigint not null,
  key                       varchar(255),
  value                     varchar(255),
  geo_location_id           bigint,
  constraint pk_properties primary key (properties_id))
;

create table required_campaign_configuration (
  required_phase_configuration_id bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  phase_definition_phase_definition_id bigint,
  configuration_config_id   bigint,
  constraint pk_required_campaign_configurati primary key (required_phase_configuration_id))
;

create table required_phase_configuration (
  required_phase_configuration_id bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  phase_definition_phase_definition_id bigint,
  config_definition_config_definition_id bigint,
  constraint pk_required_phase_configuration primary key (required_phase_configuration_id))
;

create table resource (
  resource_id               bigint not null,
  contribution_contribution_id bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  type                      integer,
  external_resource_type    varchar(255),
  url                       varchar(255),
  location_location_id      bigint,
  constraint ck_resource_type check (type in (0,1,2,3,4,5,6,7,8,9,10)),
  constraint pk_resource primary key (resource_id))
;

create table security_role (
  role_id                   bigint not null,
  name                      varchar(255),
  constraint pk_security_role primary key (role_id))
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

create table service_assembly (
  assembly_id               bigint not null,
  name                      varchar(255),
  description               varchar(255),
  city                      varchar(255),
  icon                      varchar(255),
  url                       varchar(255),
  constraint pk_service_assembly primary key (assembly_id))
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

create table service_campaign (
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
  constraint pk_service_campaign primary key (campaign_id))
;

create table service_definition (
  service_definition_id     bigint not null,
  name                      varchar(255),
  constraint pk_service_definition primary key (service_definition_id))
;

create table service_issue (
  issue_id                  bigint not null,
  title                     varchar(255),
  brief                     varchar(255),
  type                      varchar(255),
  likes                     bigint,
  assembly_assembly_id      bigint,
  resource_service_resource_id bigint,
  constraint pk_service_issue primary key (issue_id))
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
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  description               varchar(255),
  due_date                  timestamp,
  status                    integer,
  places                    varchar(255),
  constraint ck_task_status check (status in (0,1,2)),
  constraint pk_task primary key (task_id))
;

create table Token_Action (
  token_id                  bigint not null,
  token                     varchar(255),
  user_id                   bigint,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_Token_Action_type check (type in ('PR','MR','MI','EV')),
  constraint uq_Token_Action_token unique (token),
  constraint pk_Token_Action primary key (token_id))
;

create table appcivist_user (
  user_id                   bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  username                  varchar(255),
  language                  varchar(255),
  email_verified            boolean,
  profile_pic               varchar(255),
  active                    boolean,
  constraint pk_appcivist_user primary key (user_id))
;

create table user_permission (
  permission_id             bigint not null,
  permission_value          varchar(255),
  constraint pk_user_permission primary key (permission_id))
;

create table user_profile (
  profile_id                bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name                      varchar(255),
  middle_name               varchar(255),
  last_name                 varchar(255),
  birthdate                 timestamp,
  address                   varchar(255),
  constraint pk_user_profile primary key (profile_id))
;

create table working_group (
  group_id                  bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name                      varchar(255),
  text                      varchar(255),
  is_public                 boolean,
  accept_requests           boolean,
  membership_role           integer,
  constraint ck_working_group_membership_role check (membership_role in (0,1,2,3)),
  constraint pk_working_group primary key (group_id))
;


create table assembly_category (
  assembly_assembly_id           bigint not null,
  category_category_id           bigint not null,
  constraint pk_assembly_category primary key (assembly_assembly_id, category_category_id))
;

create table assembly_hashtag (
  assembly_assembly_id           bigint not null,
  hashtag_hashtag_id             bigint not null,
  constraint pk_assembly_hashtag primary key (assembly_assembly_id, hashtag_hashtag_id))
;

create table assembly_campaign (
  assembly_assembly_id           bigint not null,
  campaign_campaign_id           bigint not null,
  constraint pk_assembly_campaign primary key (assembly_assembly_id, campaign_campaign_id))
;

create table campaign_type_phase_definition (
  campaign_type_campaign_type_id bigint not null,
  phase_definition_phase_definition_id bigint not null,
  constraint pk_campaign_type_phase_definition primary key (campaign_type_campaign_type_id, phase_definition_phase_definition_id))
;

create table contribution_appcivist_user (
  contribution_contribution_id   bigint not null,
  appcivist_user_user_id         bigint not null,
  constraint pk_contribution_appcivist_user primary key (contribution_contribution_id, appcivist_user_user_id))
;

create table contribution_category (
  contribution_contribution_id   bigint not null,
  category_category_id           bigint not null,
  constraint pk_contribution_category primary key (contribution_contribution_id, category_category_id))
;

create table contribution_hashtag (
  contribution_contribution_id   bigint not null,
  hashtag_hashtag_id             bigint not null,
  constraint pk_contribution_hashtag primary key (contribution_contribution_id, hashtag_hashtag_id))
;

create table MEMBERSHIP_ROLE (
  membership_membership_id       bigint not null,
  role_role_id                   bigint not null,
  constraint pk_MEMBERSHIP_ROLE primary key (membership_membership_id, role_role_id))
;

create table RELATED_RESOURCES (
  source                         bigint not null,
  target                         bigint not null,
  constraint pk_RELATED_RESOURCES primary key (source, target))
;

create table service_campaign_service_operati (
  service_campaign_campaign_id   bigint not null,
  service_operation_service_operation_id bigint not null,
  constraint pk_service_campaign_service_operati primary key (service_campaign_campaign_id, service_operation_service_operation_id))
;

create table service_campaign_service_resourc (
  service_campaign_campaign_id   bigint not null,
  service_resource_service_resource_id bigint not null,
  constraint pk_service_campaign_service_resourc primary key (service_campaign_campaign_id, service_resource_service_resource_id))
;

create table User_Security_Roles (
  user_id                        bigint not null,
  role_id                        bigint not null,
  constraint pk_User_Security_Roles primary key (user_id, role_id))
;

create table User_User_Permission (
  user_id                        bigint not null,
  permission_id                  bigint not null,
  constraint pk_User_User_Permission primary key (user_id, permission_id))
;

create table working_group_assembly (
  working_group_group_id         bigint not null,
  assembly_assembly_id           bigint not null,
  constraint pk_working_group_assembly primary key (working_group_group_id, assembly_assembly_id))
;

create table working_group_resource (
  working_group_group_id         bigint not null,
  resource_resource_id           bigint not null,
  constraint pk_working_group_resource primary key (working_group_group_id, resource_resource_id))
;
create sequence assembly_seq;

create sequence assembly_connection_seq;

create sequence campaign_seq;

create sequence campaign_phase_seq;

create sequence campaign_phase_contribution_seq;

create sequence campaign_type_seq;

create sequence category_seq;

create sequence config_seq;

create sequence config_definition_seq;

create sequence contribution_seq;

create sequence contribution_connection_seq;

create sequence contribution_statistics_seq;

create sequence geo_seq;

create sequence geometry_seq;

create sequence hashtag_seq;

create sequence initial_data_config_seq;

create sequence Linked_Account_seq;

create sequence meeting_seq;

create sequence membership_seq;

create sequence message_seq;

create sequence module_seq;

create sequence note_seq;

create sequence phase_definition_seq;

create sequence properties_seq;

create sequence required_campaign_configuration_seq;

create sequence required_phase_configuration_seq;

create sequence resource_seq;

create sequence security_role_seq;

create sequence service_seq;

create sequence service_assembly_seq;

create sequence service_authentication_seq;

create sequence service_campaign_seq;

create sequence service_definition_seq;

create sequence service_issue_seq;

create sequence service_operation_seq;

create sequence service_operation_definition_seq;

create sequence service_parameter_seq;

create sequence service_parameter_data_model_seq;

create sequence service_parameter_definition_seq;

create sequence service_resource_seq;

create sequence task_seq;

create sequence Token_Action_seq;

create sequence appcivist_user_seq;

create sequence user_permission_seq;

create sequence user_profile_seq;

create sequence working_group_seq;

alter table assembly add constraint fk_assembly_location_1 foreign key (location_location_id) references geo (location_id);
create index ix_assembly_location_1 on assembly (location_location_id);
alter table assembly_connection add constraint fk_assembly_connection_sourceA_2 foreign key (source_assembly_assembly_id) references assembly (assembly_id);
create index ix_assembly_connection_sourceA_2 on assembly_connection (source_assembly_assembly_id);
alter table assembly_connection add constraint fk_assembly_connection_targetA_3 foreign key (target_assembly_assembly_id) references assembly (assembly_id);
create index ix_assembly_connection_targetA_3 on assembly_connection (target_assembly_assembly_id);
alter table campaign add constraint fk_campaign_assembly_4 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_campaign_assembly_4 on campaign (assembly_assembly_id);
alter table campaign add constraint fk_campaign_type_5 foreign key (type_campaign_type_id) references campaign_type (campaign_type_id);
create index ix_campaign_type_5 on campaign (type_campaign_type_id);
alter table campaign_phase add constraint fk_campaign_phase_campaign_6 foreign key (campaign_campaign_id) references campaign (campaign_id);
create index ix_campaign_phase_campaign_6 on campaign_phase (campaign_campaign_id);
alter table campaign_phase add constraint fk_campaign_phase_definition_7 foreign key (definition_phase_definition_id) references phase_definition (phase_definition_id);
create index ix_campaign_phase_definition_7 on campaign_phase (definition_phase_definition_id);
alter table campaign_phase_contribution add constraint fk_campaign_phase_contribution_8 foreign key (contribution_contribution_id) references contribution (contribution_id);
create index ix_campaign_phase_contribution_8 on campaign_phase_contribution (contribution_contribution_id);
alter table campaign_phase_contribution add constraint fk_campaign_phase_contribution_9 foreign key (phase_phase_id) references campaign_phase (phase_id);
create index ix_campaign_phase_contribution_9 on campaign_phase_contribution (phase_phase_id);
alter table campaign_phase_contribution add constraint fk_campaign_phase_contributio_10 foreign key (group_id) references working_group (group_id);
create index ix_campaign_phase_contributio_10 on campaign_phase_contribution (group_id);
alter table config add constraint fk_config_module_11 foreign key (module_mod_id) references module (mod_id);
create index ix_config_module_11 on config (module_mod_id);
alter table config add constraint fk_config_definition_12 foreign key (definition_config_definition_id) references config_definition (config_definition_id);
create index ix_config_definition_12 on config (definition_config_definition_id);
alter table config add constraint fk_config_campaign_13 foreign key (campaign_campaign_id) references campaign (campaign_id);
create index ix_config_campaign_13 on config (campaign_campaign_id);
alter table config add constraint fk_config_campaignPhase_14 foreign key (campaign_phase_phase_id) references campaign_phase (phase_id);
create index ix_config_campaignPhase_14 on config (campaign_phase_phase_id);
alter table config add constraint fk_config_assembly_15 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_config_assembly_15 on config (assembly_assembly_id);
alter table config add constraint fk_config_workingGroup_16 foreign key (working_group_group_id) references working_group (group_id);
create index ix_config_workingGroup_16 on config (working_group_group_id);
alter table contribution add constraint fk_contribution_assembly_17 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_contribution_assembly_17 on contribution (assembly_assembly_id);
alter table contribution add constraint fk_contribution_location_18 foreign key (location_location_id) references geo (location_id);
create index ix_contribution_location_18 on contribution (location_location_id);
alter table contribution add constraint fk_contribution_stats_19 foreign key (stats_contribution_statistics_id) references contribution_statistics (contribution_statistics_id);
create index ix_contribution_stats_19 on contribution (stats_contribution_statistics_id);
alter table contribution_connection add constraint fk_contribution_connection_so_20 foreign key (source_contribution_contribution_id) references contribution (contribution_id);
create index ix_contribution_connection_so_20 on contribution_connection (source_contribution_contribution_id);
alter table contribution_connection add constraint fk_contribution_connection_ta_21 foreign key (target_contribution_contribution_id) references contribution (contribution_id);
create index ix_contribution_connection_ta_21 on contribution_connection (target_contribution_contribution_id);
alter table geometry add constraint fk_geometry_geo_22 foreign key (geo_location_id) references geo (location_id);
create index ix_geometry_geo_22 on geometry (geo_location_id);
alter table Linked_Account add constraint fk_Linked_Account_user_23 foreign key (user_id) references appcivist_user (user_id);
create index ix_Linked_Account_user_23 on Linked_Account (user_id);
alter table membership add constraint fk_membership_creator_24 foreign key (creator_user_id) references appcivist_user (user_id);
create index ix_membership_creator_24 on membership (creator_user_id);
alter table membership add constraint fk_membership_user_25 foreign key (user_user_id) references appcivist_user (user_id);
create index ix_membership_user_25 on membership (user_user_id);
alter table membership add constraint fk_membership_assembly_26 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_membership_assembly_26 on membership (assembly_assembly_id);
alter table membership add constraint fk_membership_workingGroup_27 foreign key (working_group_group_id) references working_group (group_id);
create index ix_membership_workingGroup_27 on membership (working_group_group_id);
alter table message add constraint fk_message_targetUser_28 foreign key (target_user_user_id) references appcivist_user (user_id);
create index ix_message_targetUser_28 on message (target_user_user_id);
alter table message add constraint fk_message_targetWorkingGroup_29 foreign key (target_working_group_group_id) references working_group (group_id);
create index ix_message_targetWorkingGroup_29 on message (target_working_group_group_id);
alter table message add constraint fk_message_targetAssembly_30 foreign key (target_assembly_assembly_id) references assembly (assembly_id);
create index ix_message_targetAssembly_30 on message (target_assembly_assembly_id);
alter table properties add constraint fk_properties_geo_31 foreign key (geo_location_id) references geo (location_id);
create index ix_properties_geo_31 on properties (geo_location_id);
alter table required_campaign_configuration add constraint fk_required_campaign_configur_32 foreign key (phase_definition_phase_definition_id) references phase_definition (phase_definition_id);
create index ix_required_campaign_configur_32 on required_campaign_configuration (phase_definition_phase_definition_id);
alter table required_campaign_configuration add constraint fk_required_campaign_configur_33 foreign key (configuration_config_id) references config (config_id);
create index ix_required_campaign_configur_33 on required_campaign_configuration (configuration_config_id);
alter table required_phase_configuration add constraint fk_required_phase_configurati_34 foreign key (phase_definition_phase_definition_id) references phase_definition (phase_definition_id);
create index ix_required_phase_configurati_34 on required_phase_configuration (phase_definition_phase_definition_id);
alter table required_phase_configuration add constraint fk_required_phase_configurati_35 foreign key (config_definition_config_definition_id) references config_definition (config_definition_id);
create index ix_required_phase_configurati_35 on required_phase_configuration (config_definition_config_definition_id);
alter table resource add constraint fk_resource_contribution_36 foreign key (contribution_contribution_id) references contribution (contribution_id);
create index ix_resource_contribution_36 on resource (contribution_contribution_id);
alter table resource add constraint fk_resource_location_37 foreign key (location_location_id) references geo (location_id);
create index ix_resource_location_37 on resource (location_location_id);
alter table service add constraint fk_service_assembly_38 foreign key (assembly_assembly_id) references service_assembly (assembly_id);
create index ix_service_assembly_38 on service (assembly_assembly_id);
alter table service add constraint fk_service_serviceDefinition_39 foreign key (service_definition_service_definition_id) references service_definition (service_definition_id);
create index ix_service_serviceDefinition_39 on service (service_definition_service_definition_id);
alter table service_authentication add constraint fk_service_authentication_ser_40 foreign key (service_service_id) references service (service_id);
create index ix_service_authentication_ser_40 on service_authentication (service_service_id);
alter table service_campaign add constraint fk_service_campaign_previousC_41 foreign key (previous_campaign) references service_campaign (campaign_id);
create index ix_service_campaign_previousC_41 on service_campaign (previous_campaign);
alter table service_campaign add constraint fk_service_campaign_nextCampa_42 foreign key (next_campaign) references service_campaign (campaign_id);
create index ix_service_campaign_nextCampa_42 on service_campaign (next_campaign);
alter table service_campaign add constraint fk_service_campaign_issue_43 foreign key (issue_issue_id) references service_issue (issue_id);
create index ix_service_campaign_issue_43 on service_campaign (issue_issue_id);
alter table service_campaign add constraint fk_service_campaign_startOper_44 foreign key (start_operation_service_operation_id) references service_operation (service_operation_id);
create index ix_service_campaign_startOper_44 on service_campaign (start_operation_service_operation_id);
alter table service_issue add constraint fk_service_issue_assembly_45 foreign key (assembly_assembly_id) references service_assembly (assembly_id);
create index ix_service_issue_assembly_45 on service_issue (assembly_assembly_id);
alter table service_issue add constraint fk_service_issue_resource_46 foreign key (resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_issue_resource_46 on service_issue (resource_service_resource_id);
alter table service_operation add constraint fk_service_operation_definiti_47 foreign key (operation_definition_id) references service_operation_definition (operation_definition_id);
create index ix_service_operation_definiti_47 on service_operation (operation_definition_id);
alter table service_operation add constraint fk_service_operation_service_48 foreign key (service_service_id) references service (service_id);
create index ix_service_operation_service_48 on service_operation (service_service_id);
alter table service_operation_definition add constraint fk_service_operation_definiti_49 foreign key (service_definition_service_definition_id) references service_definition (service_definition_id);
create index ix_service_operation_definiti_49 on service_operation_definition (service_definition_service_definition_id);
alter table service_parameter add constraint fk_service_parameter_serviceP_50 foreign key (service_parameter_parameter_definition_id) references service_parameter_definition (parameter_definition_id);
create index ix_service_parameter_serviceP_50 on service_parameter (service_parameter_parameter_definition_id);
alter table service_parameter add constraint fk_service_parameter_serviceR_51 foreign key (service_resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_parameter_serviceR_51 on service_parameter (service_resource_service_resource_id);
alter table service_parameter add constraint fk_service_parameter_serviceO_52 foreign key (service_operation_service_operation_id) references service_operation (service_operation_id);
create index ix_service_parameter_serviceO_52 on service_parameter (service_operation_service_operation_id);
alter table service_parameter_data_model add constraint fk_service_parameter_data_mod_53 foreign key (definition_parameter_definition_id) references service_parameter_definition (parameter_definition_id);
create index ix_service_parameter_data_mod_53 on service_parameter_data_model (definition_parameter_definition_id);
alter table service_parameter_data_model add constraint fk_service_parameter_data_mod_54 foreign key (parent_data_model_data_model_id) references service_parameter_data_model (data_model_id);
create index ix_service_parameter_data_mod_54 on service_parameter_data_model (parent_data_model_data_model_id);
alter table service_parameter_definition add constraint fk_service_parameter_definiti_55 foreign key (service_operation_definition_operation_definition_id) references service_operation_definition (operation_definition_id);
create index ix_service_parameter_definiti_55 on service_parameter_definition (service_operation_definition_operation_definition_id);
alter table service_resource add constraint fk_service_resource_service_56 foreign key (service_service_id) references service (service_id);
create index ix_service_resource_service_56 on service_resource (service_service_id);
alter table service_resource add constraint fk_service_resource_parentRes_57 foreign key (parent_resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_resource_parentRes_57 on service_resource (parent_resource_service_resource_id);
alter table Token_Action add constraint fk_Token_Action_targetUser_58 foreign key (user_id) references appcivist_user (user_id);
create index ix_Token_Action_targetUser_58 on Token_Action (user_id);



alter table assembly_category add constraint fk_assembly_category_assembly_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_category add constraint fk_assembly_category_category_02 foreign key (category_category_id) references category (category_id);

alter table assembly_hashtag add constraint fk_assembly_hashtag_assembly_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_hashtag add constraint fk_assembly_hashtag_hashtag_02 foreign key (hashtag_hashtag_id) references hashtag (hashtag_id);

alter table assembly_campaign add constraint fk_assembly_campaign_assembly_01 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table assembly_campaign add constraint fk_assembly_campaign_campaign_02 foreign key (campaign_campaign_id) references campaign (campaign_id);

alter table campaign_type_phase_definition add constraint fk_campaign_type_phase_defini_01 foreign key (campaign_type_campaign_type_id) references campaign_type (campaign_type_id);

alter table campaign_type_phase_definition add constraint fk_campaign_type_phase_defini_02 foreign key (phase_definition_phase_definition_id) references phase_definition (phase_definition_id);

alter table contribution_appcivist_user add constraint fk_contribution_appcivist_use_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_appcivist_user add constraint fk_contribution_appcivist_use_02 foreign key (appcivist_user_user_id) references appcivist_user (user_id);

alter table contribution_category add constraint fk_contribution_category_cont_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_category add constraint fk_contribution_category_cate_02 foreign key (category_category_id) references category (category_id);

alter table contribution_hashtag add constraint fk_contribution_hashtag_contr_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_hashtag add constraint fk_contribution_hashtag_hasht_02 foreign key (hashtag_hashtag_id) references hashtag (hashtag_id);

alter table MEMBERSHIP_ROLE add constraint fk_MEMBERSHIP_ROLE_membership_01 foreign key (membership_membership_id) references membership (membership_id);

alter table MEMBERSHIP_ROLE add constraint fk_MEMBERSHIP_ROLE_security_r_02 foreign key (role_role_id) references security_role (role_id);

alter table RELATED_RESOURCES add constraint fk_RELATED_RESOURCES_resource_01 foreign key (source) references resource (resource_id);

alter table RELATED_RESOURCES add constraint fk_RELATED_RESOURCES_resource_02 foreign key (target) references resource (resource_id);

alter table service_campaign_service_operati add constraint fk_service_campaign_service_o_01 foreign key (service_campaign_campaign_id) references service_campaign (campaign_id);

alter table service_campaign_service_operati add constraint fk_service_campaign_service_o_02 foreign key (service_operation_service_operation_id) references service_operation (service_operation_id);

alter table service_campaign_service_resourc add constraint fk_service_campaign_service_r_01 foreign key (service_campaign_campaign_id) references service_campaign (campaign_id);

alter table service_campaign_service_resourc add constraint fk_service_campaign_service_r_02 foreign key (service_resource_service_resource_id) references service_resource (service_resource_id);

alter table User_Security_Roles add constraint fk_User_Security_Roles_appciv_01 foreign key (user_id) references appcivist_user (user_id);

alter table User_Security_Roles add constraint fk_User_Security_Roles_securi_02 foreign key (role_id) references security_role (role_id);

alter table User_User_Permission add constraint fk_User_User_Permission_appci_01 foreign key (user_id) references appcivist_user (user_id);

alter table User_User_Permission add constraint fk_User_User_Permission_user__02 foreign key (permission_id) references user_permission (permission_id);

alter table working_group_assembly add constraint fk_working_group_assembly_wor_01 foreign key (working_group_group_id) references working_group (group_id);

alter table working_group_assembly add constraint fk_working_group_assembly_ass_02 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table working_group_resource add constraint fk_working_group_resource_wor_01 foreign key (working_group_group_id) references working_group (group_id);

alter table working_group_resource add constraint fk_working_group_resource_res_02 foreign key (resource_resource_id) references resource (resource_id);

# --- !Downs

drop table if exists assembly cascade;

drop table if exists assembly_category cascade;

drop table if exists assembly_hashtag cascade;

drop table if exists assembly_campaign cascade;

drop table if exists assembly_connection cascade;

drop table if exists campaign cascade;

drop table if exists campaign_phase cascade;

drop table if exists campaign_phase_contribution cascade;

drop table if exists campaign_type cascade;

drop table if exists campaign_type_phase_definition cascade;

drop table if exists category cascade;

drop table if exists config cascade;

drop table if exists config_definition cascade;

drop table if exists contribution cascade;

drop table if exists contribution_appcivist_user cascade;

drop table if exists contribution_category cascade;

drop table if exists contribution_hashtag cascade;

drop table if exists contribution_connection cascade;

drop table if exists contribution_statistics cascade;

drop table if exists geo cascade;

drop table if exists geometry cascade;

drop table if exists hashtag cascade;

drop table if exists initial_data_config cascade;

drop table if exists Linked_Account cascade;

drop table if exists meeting cascade;

drop table if exists membership cascade;

drop table if exists MEMBERSHIP_ROLE cascade;

drop table if exists message cascade;

drop table if exists module cascade;

drop table if exists note cascade;

drop table if exists phase_definition cascade;

drop table if exists properties cascade;

drop table if exists required_campaign_configuration cascade;

drop table if exists required_phase_configuration cascade;

drop table if exists resource cascade;

drop table if exists RELATED_RESOURCES cascade;

drop table if exists security_role cascade;

drop table if exists service cascade;

drop table if exists service_assembly cascade;

drop table if exists service_authentication cascade;

drop table if exists service_campaign cascade;

drop table if exists service_campaign_service_operati cascade;

drop table if exists service_campaign_service_resourc cascade;

drop table if exists service_definition cascade;

drop table if exists service_issue cascade;

drop table if exists service_operation cascade;

drop table if exists service_operation_definition cascade;

drop table if exists service_parameter cascade;

drop table if exists service_parameter_data_model cascade;

drop table if exists service_parameter_definition cascade;

drop table if exists service_resource cascade;

drop table if exists task cascade;

drop table if exists Token_Action cascade;

drop table if exists appcivist_user cascade;

drop table if exists User_Security_Roles cascade;

drop table if exists User_User_Permission cascade;

drop table if exists user_permission cascade;

drop table if exists user_profile cascade;

drop table if exists working_group cascade;

drop table if exists working_group_assembly cascade;

drop table if exists working_group_resource cascade;

drop sequence if exists assembly_seq;

drop sequence if exists assembly_connection_seq;

drop sequence if exists campaign_seq;

drop sequence if exists campaign_phase_seq;

drop sequence if exists campaign_phase_contribution_seq;

drop sequence if exists campaign_type_seq;

drop sequence if exists category_seq;

drop sequence if exists config_seq;

drop sequence if exists config_definition_seq;

drop sequence if exists contribution_seq;

drop sequence if exists contribution_connection_seq;

drop sequence if exists contribution_statistics_seq;

drop sequence if exists geo_seq;

drop sequence if exists geometry_seq;

drop sequence if exists hashtag_seq;

drop sequence if exists initial_data_config_seq;

drop sequence if exists Linked_Account_seq;

drop sequence if exists meeting_seq;

drop sequence if exists membership_seq;

drop sequence if exists message_seq;

drop sequence if exists module_seq;

drop sequence if exists note_seq;

drop sequence if exists phase_definition_seq;

drop sequence if exists properties_seq;

drop sequence if exists required_campaign_configuration_seq;

drop sequence if exists required_phase_configuration_seq;

drop sequence if exists resource_seq;

drop sequence if exists security_role_seq;

drop sequence if exists service_seq;

drop sequence if exists service_assembly_seq;

drop sequence if exists service_authentication_seq;

drop sequence if exists service_campaign_seq;

drop sequence if exists service_definition_seq;

drop sequence if exists service_issue_seq;

drop sequence if exists service_operation_seq;

drop sequence if exists service_operation_definition_seq;

drop sequence if exists service_parameter_seq;

drop sequence if exists service_parameter_data_model_seq;

drop sequence if exists service_parameter_definition_seq;

drop sequence if exists service_resource_seq;

drop sequence if exists task_seq;

drop sequence if exists Token_Action_seq;

drop sequence if exists appcivist_user_seq;

drop sequence if exists user_permission_seq;

drop sequence if exists user_profile_seq;

drop sequence if exists working_group_seq;

