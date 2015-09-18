# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table assembly (
  assembly_id               bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  name                      varchar(255),
  shortname                 varchar(255),
  description               varchar(255),
  url                       varchar(255),
  listed                    boolean,
  assembly_profile_id       bigint,
  location_location_id      bigint,
  resources_uuid            varchar(40),
  forum_uuid                varchar(40),
  constraint uq_assembly_assembly_profile_id unique (assembly_profile_id),
  constraint uq_assembly_resources_uuid unique (resources_uuid),
  constraint uq_assembly_forum_uuid unique (forum_uuid),
  constraint pk_assembly primary key (assembly_id))
;

create table assembly_profile (
  assembly_profile_id       bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  assembly                  bigint,
  target_audience           varchar(255),
  supported_membership      varchar(22),
  management_type           varchar(25),
  icon                      varchar(255),
  cover                     varchar(255),
  primary_contact_name      varchar(255),
  primary_contact_phone     varchar(255),
  primary_contact_email     varchar(255),
  constraint ck_assembly_profile_supported_membership check (supported_membership in ('OPEN','INVITATION','REQUEST','INVITATION_AND_REQUEST')),
  constraint ck_assembly_profile_management_type check (management_type in ('OPEN','COORDINATED','MODERATED','COORDINATED_AND_MODERATED','DEMOCRATIC')),
  constraint uq_assembly_profile_assembly unique (assembly),
  constraint pk_assembly_profile primary key (assembly_profile_id))
;

create table campaign (
  campaign_id               bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  shortname                 varchar(255),
  goal                      varchar(255),
  active                    boolean,
  url                       varchar(255),
  uuid                      varchar(40),
  visibility                varchar(6),
  resources_uuid            varchar(40),
  type_campaign_type_id     bigint,
  constraint ck_campaign_visibility check (visibility in ('PUBLIC','HIDDEN')),
  constraint uq_campaign_resources_uuid unique (resources_uuid),
  constraint pk_campaign primary key (campaign_id))
;

create table campaign_phase (
  phase_id                  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  start_date                timestamp,
  end_date                  timestamp,
  uuid                      varchar(40),
  campaign_campaign_id      bigint,
  definition_phase_definition_id bigint,
  resources_uuid            varchar(40),
  can_overlap               boolean,
  constraint uq_campaign_phase_resources_uuid unique (resources_uuid),
  constraint pk_campaign_phase primary key (phase_id))
;

create table campaign_phase_contribution (
  campaign_phase_contribution_id bigserial not null,
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
  constraint uq_campaign_phase_contribution_g unique (group_id),
  constraint pk_campaign_phase_contribution primary key (campaign_phase_contribution_id))
;

create table campaign_phase_milestone (
  campaign_phase_milestone_id bigserial not null,
  campaign_phase_phase_id   bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  start                     timestamp,
  days                      integer,
  uuid                      varchar(40),
  constraint pk_campaign_phase_milestone primary key (campaign_phase_milestone_id))
;

create table campaign_type (
  campaign_type_id          bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name_key                  varchar(23),
  name                      varchar(255),
  constraint ck_campaign_type_name_key check (name_key in ('PARTICIPATORY_BUDGETING','OCCUPY_ACTION','AWARENESS_RAISING','ACTION_PROMOTION','MOBILIZATION','FUNDRAISING')),
  constraint pk_campaign_type primary key (campaign_type_id))
;

create table config (
  uuid                      varchar(40) not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  key                       varchar(255),
  value                     varchar(255),
  config_target             varchar(13),
  target_uuid               varchar(40),
  definition_uuid           varchar(40),
  constraint ck_config_config_target check (config_target in ('ASSEMBLY','CAMPAIGN','PHASE','WORKING_GROUP','MODULE','PROPOSAL','CONTRIBUTION')),
  constraint pk_config primary key (uuid))
;

create table config_definition (
  uuid                      varchar(40) not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  key                       varchar(255),
  value_type                varchar(255),
  description               varchar(255),
  default_value             varchar(255),
  config_target             varchar(13),
  constraint ck_config_definition_config_target check (config_target in ('ASSEMBLY','CAMPAIGN','PHASE','WORKING_GROUP','MODULE','PROPOSAL','CONTRIBUTION')),
  constraint uq_config_definition_1 unique (key),
  constraint pk_config_definition primary key (uuid))
;

create table contribution (
  type                      varchar(31) not null,
  contribution_id           bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  title                     varchar(255),
  text                      varchar(255),
  author_user_id            bigint,
  assembly_assembly_id      bigint,
  location_location_id      bigint,
  stats_contribution_statistics_id bigint,
  proposal_pad_resource_id  bigint,
  proposal_template_resource_id bigint,
  constraint uq_contribution_location_locatio unique (location_location_id),
  constraint uq_contribution_stats_contributi unique (stats_contribution_statistics_id),
  constraint uq_contribution_proposal_pad_res unique (proposal_pad_resource_id),
  constraint uq_contribution_proposal_templat unique (proposal_template_resource_id),
  constraint pk_contribution primary key (contribution_id))
;

create table contribution_connection (
  uuid                      varchar(40) not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  type                      integer,
  source_uuid               varchar(40),
  target_uuid               varchar(40),
  constraint ck_contribution_connection_type check (type in (0,1,2,3,4)),
  constraint pk_contribution_connection primary key (uuid))
;

create table contribution_statistics (
  contribution_statistics_id bigserial not null,
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
  location_id               bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  type                      varchar(255),
  constraint pk_geo primary key (location_id))
;

create table geometry (
  geometry_id               bigserial not null,
  type                      integer,
  coordinates               varchar(255),
  geo_location_id           bigint,
  constraint ck_geometry_type check (type in (0,1,2,3,4,5)),
  constraint pk_geometry primary key (geometry_id))
;

create table hashtag (
  hashtag_id                bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  hashtag                   varchar(255),
  constraint pk_hashtag primary key (hashtag_id))
;

create table initial_data_config (
  data_file_id              bigserial not null,
  data_file                 varchar(255),
  loaded                    boolean,
  constraint pk_initial_data_config primary key (data_file_id))
;

create table Linked_Account (
  account_id                bigserial not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_Linked_Account primary key (account_id))
;

create table location (
  location_id               bigserial not null,
  place_name                varchar(255),
  street                    varchar(255),
  city                      varchar(255),
  state                     varchar(255),
  zip                       varchar(255),
  country                   varchar(255),
  serialized_location       varchar(255),
  geo_json                  TEXT,
  constraint pk_location primary key (location_id))
;

create table meeting (
  meeting_id                bigserial not null,
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
  membership_type           varchar(31) not null,
  membership_id             bigserial not null,
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
  message_id                bigserial not null,
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

create table note (
  note_id                   bigserial not null,
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
  phase_definition_id       bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name                      varchar(255),
  constraint pk_phase_definition primary key (phase_definition_id))
;

create table properties (
  properties_id             bigserial not null,
  key                       varchar(255),
  value                     varchar(255),
  geo_location_id           bigint,
  constraint pk_properties primary key (properties_id))
;

create table required_campaign_configuration (
  required_campaign_configuration_id bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  campaign_type_campaign_type_id bigint,
  config_definition_uuid    varchar(40),
  constraint pk_required_campaign_configurati primary key (required_campaign_configuration_id))
;

create table required_campaign_phase_milestone (
  campaign_phase_milestone_id bigserial not null,
  phase_definition_phase_definition_id bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  constraint pk_required_campaign_phase_miles primary key (campaign_phase_milestone_id))
;

create table required_phase_configuration (
  required_phase_configuration_id bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  phase_definition_phase_definition_id bigint,
  config_definition_uuid    varchar(40),
  constraint pk_required_phase_configuration primary key (required_phase_configuration_id))
;

create table resource (
  resource_type             varchar(31) not null,
  resource_id               bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  url                       varchar(255),
  pad_id                    varchar(255),
  url_large                 varchar(255),
  url_medium                varchar(255),
  url_thumbnail             varchar(255),
  constraint pk_resource primary key (resource_id))
;

create table resource_space (
  uuid                      varchar(40) not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  type                      varchar(14),
  parent                    varchar(40),
  constraint ck_resource_space_type check (type in ('ASSEMBLY','CAMPAIGN','WORKING_GROUP','CAMPAIGN_PHASE','CONTRIBUTION')),
  constraint pk_resource_space primary key (uuid))
;

create table security_role (
  role_id                   bigserial not null,
  name                      varchar(255),
  constraint pk_security_role primary key (role_id))
;

create table service (
  service_id                bigserial not null,
  name                      varchar(255),
  base_url                  varchar(255),
  assembly_assembly_id      bigint,
  service_definition_service_definition_id bigint,
  trailing_slash            boolean,
  constraint pk_service primary key (service_id))
;

create table service_assembly (
  assembly_id               bigserial not null,
  name                      varchar(255),
  description               varchar(255),
  city                      varchar(255),
  icon                      varchar(255),
  url                       varchar(255),
  constraint pk_service_assembly primary key (assembly_id))
;

create table service_authentication (
  service_authentication_id bigserial not null,
  auth_type                 varchar(255),
  token                     varchar(2048),
  token_injection           varchar(255),
  token_param_name          varchar(255),
  service_service_id        bigint,
  constraint pk_service_authentication primary key (service_authentication_id))
;

create table service_campaign (
  campaign_id               bigserial not null,
  name                      varchar(255),
  url                       varchar(255),
  start_date                varchar(255),
  end_date                  varchar(255),
  enabled                   boolean,
  previous_campaign         bigint,
  next_campaign             bigint,
  issue_issue_id            bigint,
  start_operation_service_operation_id bigint,
  start_operation_type      varchar(255),
  constraint uq_service_campaign_previous_cam unique (previous_campaign),
  constraint uq_service_campaign_next_campaig unique (next_campaign),
  constraint pk_service_campaign primary key (campaign_id))
;

create table service_definition (
  service_definition_id     bigserial not null,
  name                      varchar(255),
  constraint pk_service_definition primary key (service_definition_id))
;

create table service_issue (
  issue_id                  bigserial not null,
  title                     varchar(255),
  brief                     varchar(255),
  type                      varchar(255),
  likes                     bigint,
  assembly_assembly_id      bigint,
  resource_service_resource_id bigint,
  constraint pk_service_issue primary key (issue_id))
;

create table service_operation (
  service_operation_id      bigserial not null,
  app_civist_operation      varchar(255),
  expected_resource         varchar(255),
  operation_definition_id   bigint,
  service_service_id        bigint,
  constraint uq_service_operation_operation_d unique (operation_definition_id),
  constraint pk_service_operation primary key (service_operation_id))
;

create table service_operation_definition (
  operation_definition_id   bigserial not null,
  name                      varchar(255),
  type                      varchar(255),
  method                    varchar(255),
  service_definition_service_definition_id bigint,
  name_on_path              boolean,
  constraint uq_service_operation_definition_ unique (service_definition_service_definition_id),
  constraint pk_service_operation_definition primary key (operation_definition_id))
;

create table service_parameter (
  service_parameter_id      bigserial not null,
  value                     varchar(255),
  service_parameter_parameter_definition_id bigint,
  service_resource_service_resource_id bigint,
  service_operation_service_operation_id bigint,
  constraint pk_service_parameter primary key (service_parameter_id))
;

create table service_parameter_data_model (
  data_model_id             bigserial not null,
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
  parameter_definition_id   bigserial not null,
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
  service_resource_id       bigserial not null,
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
  task_id                   bigserial not null,
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

create table theme (
  theme_id                  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  description               varchar(255),
  icon                      varchar(255),
  cover                     varchar(255),
  constraint pk_theme primary key (theme_id))
;

create table Token_Action (
  token_id                  bigserial not null,
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
  user_id                   bigserial not null,
  uuid                      varchar(40),
  email                     varchar(255),
  name                      varchar(255),
  username                  varchar(255),
  language                  varchar(255),
  email_verified            boolean,
  profile_pic_resource_id   bigint,
  active                    boolean,
  constraint uq_appcivist_user_profile_pic_re unique (profile_pic_resource_id),
  constraint pk_appcivist_user primary key (user_id))
;

create table user_permission (
  permission_id             bigserial not null,
  permission_value          varchar(255),
  constraint pk_user_permission primary key (permission_id))
;

create table user_profile (
  profile_id                bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  name                      varchar(255),
  middle_name               varchar(255),
  last_name                 varchar(255),
  birthdate                 timestamp,
  address                   varchar(255),
  user_user_id              bigint,
  constraint uq_user_profile_user_user_id unique (user_user_id),
  constraint pk_user_profile primary key (profile_id))
;

create table working_group (
  group_id                  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  name                      varchar(255),
  text                      varchar(255),
  is_public                 boolean,
  accept_requests           boolean,
  management_type           varchar(25),
  resources_uuid            varchar(40),
  constraint ck_working_group_management_type check (management_type in ('OPEN','COORDINATED','MODERATED','COORDINATED_AND_MODERATED','DEMOCRATIC')),
  constraint uq_working_group_resources_uuid unique (resources_uuid),
  constraint pk_working_group primary key (group_id))
;


create table campaign_type_default_phases (
  campaign_type_campaign_type_id bigint not null,
  phase_definition_phase_definition_id bigint not null,
  constraint pk_campaign_type_default_phases primary key (campaign_type_campaign_type_id, phase_definition_phase_definition_id))
;

create table campaign_type_required_configs (
  campaign_type_campaign_type_id bigint not null,
  required_campaign_configuration_required_campaign_configuration_id bigint not null,
  constraint pk_campaign_type_required_configs primary key (campaign_type_campaign_type_id, required_campaign_configuration_required_campaign_configuration_id))
;

create table contribution_appcivist_user (
  contribution_contribution_id   bigint not null,
  appcivist_user_user_id         bigint not null,
  constraint pk_contribution_appcivist_user primary key (contribution_contribution_id, appcivist_user_user_id))
;

create table contribution_theme (
  contribution_contribution_id   bigint not null,
  theme_theme_id                 bigint not null,
  constraint pk_contribution_theme primary key (contribution_contribution_id, theme_theme_id))
;

create table contribution_resource (
  contribution_contribution_id   bigint not null,
  resource_resource_id           bigint not null,
  constraint pk_contribution_resource primary key (contribution_contribution_id, resource_resource_id))
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

create table resource_space_config (
  resource_space_uuid            varchar(40) not null,
  config_uuid                    varchar(40) not null,
  constraint pk_resource_space_config primary key (resource_space_uuid, config_uuid))
;

create table resource_space_theme (
  resource_space_uuid            varchar(40) not null,
  theme_theme_id                 bigint not null,
  constraint pk_resource_space_theme primary key (resource_space_uuid, theme_theme_id))
;

create table resource_space_campaign (
  resource_space_uuid            varchar(40) not null,
  campaign_campaign_id           bigint not null,
  constraint pk_resource_space_campaign primary key (resource_space_uuid, campaign_campaign_id))
;

create table resource_space_campaign_phases (
  resource_space_uuid            varchar(40) not null,
  campaign_phase_phase_id        bigint not null,
  constraint pk_resource_space_campaign_phases primary key (resource_space_uuid, campaign_phase_phase_id))
;

create table resource_space_working_groups (
  resource_space_uuid            varchar(40) not null,
  working_group_group_id         bigint not null,
  constraint pk_resource_space_working_groups primary key (resource_space_uuid, working_group_group_id))
;

create table resource_space_contributions (
  resource_space_uuid            varchar(40) not null,
  contribution_contribution_id   bigint not null,
  constraint pk_resource_space_contributions primary key (resource_space_uuid, contribution_contribution_id))
;

create table resource_space_assemblies (
  resource_space_uuid            varchar(40) not null,
  assembly_assembly_id           bigint not null,
  constraint pk_resource_space_assemblies primary key (resource_space_uuid, assembly_assembly_id))
;

create table resource_space_resource (
  resource_space_uuid            varchar(40) not null,
  resource_resource_id           bigint not null,
  constraint pk_resource_space_resource primary key (resource_space_uuid, resource_resource_id))
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
alter table assembly add constraint fk_assembly_profile_1 foreign key (assembly_profile_id) references assembly_profile (assembly_profile_id);
create index ix_assembly_profile_1 on assembly (assembly_profile_id);
alter table assembly add constraint fk_assembly_location_2 foreign key (location_location_id) references location (location_id);
create index ix_assembly_location_2 on assembly (location_location_id);
alter table assembly add constraint fk_assembly_resources_3 foreign key (resources_uuid) references resource_space (uuid);
create index ix_assembly_resources_3 on assembly (resources_uuid);
alter table assembly add constraint fk_assembly_forum_4 foreign key (forum_uuid) references resource_space (uuid);
create index ix_assembly_forum_4 on assembly (forum_uuid);
alter table assembly_profile add constraint fk_assembly_profile_assembly_5 foreign key (assembly) references assembly (assembly_id);
create index ix_assembly_profile_assembly_5 on assembly_profile (assembly);
alter table campaign add constraint fk_campaign_resources_6 foreign key (resources_uuid) references resource_space (uuid);
create index ix_campaign_resources_6 on campaign (resources_uuid);
alter table campaign add constraint fk_campaign_type_7 foreign key (type_campaign_type_id) references campaign_type (campaign_type_id);
create index ix_campaign_type_7 on campaign (type_campaign_type_id);
alter table campaign_phase add constraint fk_campaign_phase_campaign_8 foreign key (campaign_campaign_id) references campaign (campaign_id);
create index ix_campaign_phase_campaign_8 on campaign_phase (campaign_campaign_id);
alter table campaign_phase add constraint fk_campaign_phase_definition_9 foreign key (definition_phase_definition_id) references phase_definition (phase_definition_id);
create index ix_campaign_phase_definition_9 on campaign_phase (definition_phase_definition_id);
alter table campaign_phase add constraint fk_campaign_phase_resources_10 foreign key (resources_uuid) references resource_space (uuid);
create index ix_campaign_phase_resources_10 on campaign_phase (resources_uuid);
alter table campaign_phase_contribution add constraint fk_campaign_phase_contributio_11 foreign key (contribution_contribution_id) references contribution (contribution_id);
create index ix_campaign_phase_contributio_11 on campaign_phase_contribution (contribution_contribution_id);
alter table campaign_phase_contribution add constraint fk_campaign_phase_contributio_12 foreign key (phase_phase_id) references campaign_phase (phase_id);
create index ix_campaign_phase_contributio_12 on campaign_phase_contribution (phase_phase_id);
alter table campaign_phase_contribution add constraint fk_campaign_phase_contributio_13 foreign key (group_id) references working_group (group_id);
create index ix_campaign_phase_contributio_13 on campaign_phase_contribution (group_id);
alter table campaign_phase_milestone add constraint fk_campaign_phase_milestone_c_14 foreign key (campaign_phase_phase_id) references campaign_phase (phase_id);
create index ix_campaign_phase_milestone_c_14 on campaign_phase_milestone (campaign_phase_phase_id);
alter table config add constraint fk_config_definition_15 foreign key (definition_uuid) references config_definition (uuid);
create index ix_config_definition_15 on config (definition_uuid);
alter table contribution add constraint fk_contribution_author_16 foreign key (author_user_id) references appcivist_user (user_id);
create index ix_contribution_author_16 on contribution (author_user_id);
alter table contribution add constraint fk_contribution_assembly_17 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_contribution_assembly_17 on contribution (assembly_assembly_id);
alter table contribution add constraint fk_contribution_location_18 foreign key (location_location_id) references location (location_id);
create index ix_contribution_location_18 on contribution (location_location_id);
alter table contribution add constraint fk_contribution_stats_19 foreign key (stats_contribution_statistics_id) references contribution_statistics (contribution_statistics_id);
create index ix_contribution_stats_19 on contribution (stats_contribution_statistics_id);
alter table contribution add constraint fk_contribution_proposalPad_20 foreign key (proposal_pad_resource_id) references resource (resource_id);
create index ix_contribution_proposalPad_20 on contribution (proposal_pad_resource_id);
alter table contribution add constraint fk_contribution_proposalTempl_21 foreign key (proposal_template_resource_id) references resource (resource_id);
create index ix_contribution_proposalTempl_21 on contribution (proposal_template_resource_id);
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
alter table required_campaign_configuration add constraint fk_required_campaign_configur_32 foreign key (campaign_type_campaign_type_id) references campaign_type (campaign_type_id);
create index ix_required_campaign_configur_32 on required_campaign_configuration (campaign_type_campaign_type_id);
alter table required_campaign_configuration add constraint fk_required_campaign_configur_33 foreign key (config_definition_uuid) references config_definition (uuid);
create index ix_required_campaign_configur_33 on required_campaign_configuration (config_definition_uuid);
alter table required_campaign_phase_milestone add constraint fk_required_campaign_phase_mi_34 foreign key (phase_definition_phase_definition_id) references phase_definition (phase_definition_id);
create index ix_required_campaign_phase_mi_34 on required_campaign_phase_milestone (phase_definition_phase_definition_id);
alter table required_phase_configuration add constraint fk_required_phase_configurati_35 foreign key (phase_definition_phase_definition_id) references phase_definition (phase_definition_id);
create index ix_required_phase_configurati_35 on required_phase_configuration (phase_definition_phase_definition_id);
alter table required_phase_configuration add constraint fk_required_phase_configurati_36 foreign key (config_definition_uuid) references config_definition (uuid);
create index ix_required_phase_configurati_36 on required_phase_configuration (config_definition_uuid);
alter table service add constraint fk_service_assembly_37 foreign key (assembly_assembly_id) references service_assembly (assembly_id);
create index ix_service_assembly_37 on service (assembly_assembly_id);
alter table service add constraint fk_service_serviceDefinition_38 foreign key (service_definition_service_definition_id) references service_definition (service_definition_id);
create index ix_service_serviceDefinition_38 on service (service_definition_service_definition_id);
alter table service_authentication add constraint fk_service_authentication_ser_39 foreign key (service_service_id) references service (service_id);
create index ix_service_authentication_ser_39 on service_authentication (service_service_id);
alter table service_campaign add constraint fk_service_campaign_previousC_40 foreign key (previous_campaign) references service_campaign (campaign_id);
create index ix_service_campaign_previousC_40 on service_campaign (previous_campaign);
alter table service_campaign add constraint fk_service_campaign_nextCampa_41 foreign key (next_campaign) references service_campaign (campaign_id);
create index ix_service_campaign_nextCampa_41 on service_campaign (next_campaign);
alter table service_campaign add constraint fk_service_campaign_issue_42 foreign key (issue_issue_id) references service_issue (issue_id);
create index ix_service_campaign_issue_42 on service_campaign (issue_issue_id);
alter table service_campaign add constraint fk_service_campaign_startOper_43 foreign key (start_operation_service_operation_id) references service_operation (service_operation_id);
create index ix_service_campaign_startOper_43 on service_campaign (start_operation_service_operation_id);
alter table service_issue add constraint fk_service_issue_assembly_44 foreign key (assembly_assembly_id) references service_assembly (assembly_id);
create index ix_service_issue_assembly_44 on service_issue (assembly_assembly_id);
alter table service_issue add constraint fk_service_issue_resource_45 foreign key (resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_issue_resource_45 on service_issue (resource_service_resource_id);
alter table service_operation add constraint fk_service_operation_definiti_46 foreign key (operation_definition_id) references service_operation_definition (operation_definition_id);
create index ix_service_operation_definiti_46 on service_operation (operation_definition_id);
alter table service_operation add constraint fk_service_operation_service_47 foreign key (service_service_id) references service (service_id);
create index ix_service_operation_service_47 on service_operation (service_service_id);
alter table service_operation_definition add constraint fk_service_operation_definiti_48 foreign key (service_definition_service_definition_id) references service_definition (service_definition_id);
create index ix_service_operation_definiti_48 on service_operation_definition (service_definition_service_definition_id);
alter table service_parameter add constraint fk_service_parameter_serviceP_49 foreign key (service_parameter_parameter_definition_id) references service_parameter_definition (parameter_definition_id);
create index ix_service_parameter_serviceP_49 on service_parameter (service_parameter_parameter_definition_id);
alter table service_parameter add constraint fk_service_parameter_serviceR_50 foreign key (service_resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_parameter_serviceR_50 on service_parameter (service_resource_service_resource_id);
alter table service_parameter add constraint fk_service_parameter_serviceO_51 foreign key (service_operation_service_operation_id) references service_operation (service_operation_id);
create index ix_service_parameter_serviceO_51 on service_parameter (service_operation_service_operation_id);
alter table service_parameter_data_model add constraint fk_service_parameter_data_mod_52 foreign key (definition_parameter_definition_id) references service_parameter_definition (parameter_definition_id);
create index ix_service_parameter_data_mod_52 on service_parameter_data_model (definition_parameter_definition_id);
alter table service_parameter_data_model add constraint fk_service_parameter_data_mod_53 foreign key (parent_data_model_data_model_id) references service_parameter_data_model (data_model_id);
create index ix_service_parameter_data_mod_53 on service_parameter_data_model (parent_data_model_data_model_id);
alter table service_parameter_definition add constraint fk_service_parameter_definiti_54 foreign key (service_operation_definition_operation_definition_id) references service_operation_definition (operation_definition_id);
create index ix_service_parameter_definiti_54 on service_parameter_definition (service_operation_definition_operation_definition_id);
alter table service_resource add constraint fk_service_resource_service_55 foreign key (service_service_id) references service (service_id);
create index ix_service_resource_service_55 on service_resource (service_service_id);
alter table service_resource add constraint fk_service_resource_parentRes_56 foreign key (parent_resource_service_resource_id) references service_resource (service_resource_id);
create index ix_service_resource_parentRes_56 on service_resource (parent_resource_service_resource_id);
alter table Token_Action add constraint fk_Token_Action_targetUser_57 foreign key (user_id) references appcivist_user (user_id);
create index ix_Token_Action_targetUser_57 on Token_Action (user_id);
alter table appcivist_user add constraint fk_appcivist_user_profilePic_58 foreign key (profile_pic_resource_id) references resource (resource_id);
create index ix_appcivist_user_profilePic_58 on appcivist_user (profile_pic_resource_id);
alter table user_profile add constraint fk_user_profile_user_59 foreign key (user_user_id) references appcivist_user (user_id);
create index ix_user_profile_user_59 on user_profile (user_user_id);
alter table working_group add constraint fk_working_group_resources_60 foreign key (resources_uuid) references resource_space (uuid);
create index ix_working_group_resources_60 on working_group (resources_uuid);



alter table campaign_type_default_phases add constraint fk_campaign_type_default_phas_01 foreign key (campaign_type_campaign_type_id) references campaign_type (campaign_type_id);

alter table campaign_type_default_phases add constraint fk_campaign_type_default_phas_02 foreign key (phase_definition_phase_definition_id) references phase_definition (phase_definition_id);

alter table campaign_type_required_configs add constraint fk_campaign_type_required_con_01 foreign key (campaign_type_campaign_type_id) references campaign_type (campaign_type_id);

alter table campaign_type_required_configs add constraint fk_campaign_type_required_con_02 foreign key (required_campaign_configuration_required_campaign_configuration_id) references required_campaign_configuration (required_campaign_configuration_id);

alter table contribution_appcivist_user add constraint fk_contribution_appcivist_use_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_appcivist_user add constraint fk_contribution_appcivist_use_02 foreign key (appcivist_user_user_id) references appcivist_user (user_id);

alter table contribution_theme add constraint fk_contribution_theme_contrib_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_theme add constraint fk_contribution_theme_theme_02 foreign key (theme_theme_id) references theme (theme_id);

alter table contribution_resource add constraint fk_contribution_resource_cont_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_resource add constraint fk_contribution_resource_reso_02 foreign key (resource_resource_id) references resource (resource_id);

alter table contribution_hashtag add constraint fk_contribution_hashtag_contr_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_hashtag add constraint fk_contribution_hashtag_hasht_02 foreign key (hashtag_hashtag_id) references hashtag (hashtag_id);

alter table MEMBERSHIP_ROLE add constraint fk_MEMBERSHIP_ROLE_membership_01 foreign key (membership_membership_id) references membership (membership_id);

alter table MEMBERSHIP_ROLE add constraint fk_MEMBERSHIP_ROLE_security_r_02 foreign key (role_role_id) references security_role (role_id);

alter table resource_space_config add constraint fk_resource_space_config_reso_01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_config add constraint fk_resource_space_config_conf_02 foreign key (config_uuid) references config (uuid);

alter table resource_space_theme add constraint fk_resource_space_theme_resou_01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_theme add constraint fk_resource_space_theme_theme_02 foreign key (theme_theme_id) references theme (theme_id);

alter table resource_space_campaign add constraint fk_resource_space_campaign_re_01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_campaign add constraint fk_resource_space_campaign_ca_02 foreign key (campaign_campaign_id) references campaign (campaign_id);

alter table resource_space_campaign_phases add constraint fk_resource_space_campaign_ph_01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_campaign_phases add constraint fk_resource_space_campaign_ph_02 foreign key (campaign_phase_phase_id) references campaign_phase (phase_id);

alter table resource_space_working_groups add constraint fk_resource_space_working_gro_01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_working_groups add constraint fk_resource_space_working_gro_02 foreign key (working_group_group_id) references working_group (group_id);

alter table resource_space_contributions add constraint fk_resource_space_contributio_01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_contributions add constraint fk_resource_space_contributio_02 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table resource_space_assemblies add constraint fk_resource_space_assemblies__01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_assemblies add constraint fk_resource_space_assemblies__02 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table resource_space_resource add constraint fk_resource_space_resource_re_01 foreign key (resource_space_uuid) references resource_space (uuid);

alter table resource_space_resource add constraint fk_resource_space_resource_re_02 foreign key (resource_resource_id) references resource (resource_id);

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
create index ix_location_serialized_locati_61 on location(serialized_location);

# --- !Downs

drop table if exists assembly cascade;

drop table if exists assembly_profile cascade;

drop table if exists campaign cascade;

drop table if exists campaign_phase cascade;

drop table if exists campaign_phase_contribution cascade;

drop table if exists campaign_phase_milestone cascade;

drop table if exists campaign_type cascade;

drop table if exists campaign_type_default_phases cascade;

drop table if exists campaign_type_required_configs cascade;

drop table if exists config cascade;

drop table if exists config_definition cascade;

drop table if exists contribution cascade;

drop table if exists contribution_appcivist_user cascade;

drop table if exists contribution_theme cascade;

drop table if exists contribution_resource cascade;

drop table if exists contribution_hashtag cascade;

drop table if exists contribution_connection cascade;

drop table if exists contribution_statistics cascade;

drop table if exists geo cascade;

drop table if exists geometry cascade;

drop table if exists hashtag cascade;

drop table if exists initial_data_config cascade;

drop table if exists Linked_Account cascade;

drop table if exists location cascade;

drop table if exists meeting cascade;

drop table if exists membership cascade;

drop table if exists MEMBERSHIP_ROLE cascade;

drop table if exists message cascade;

drop table if exists note cascade;

drop table if exists phase_definition cascade;

drop table if exists properties cascade;

drop table if exists required_campaign_configuration cascade;

drop table if exists required_campaign_phase_milestone cascade;

drop table if exists required_phase_configuration cascade;

drop table if exists resource cascade;

drop table if exists resource_space cascade;

drop table if exists resource_space_config cascade;

drop table if exists resource_space_theme cascade;

drop table if exists resource_space_campaign cascade;

drop table if exists resource_space_campaign_phases cascade;

drop table if exists resource_space_working_groups cascade;

drop table if exists resource_space_contributions cascade;

drop table if exists resource_space_assemblies cascade;

drop table if exists resource_space_resource cascade;

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

drop table if exists theme cascade;

drop table if exists Token_Action cascade;

drop table if exists appcivist_user cascade;

drop table if exists User_Security_Roles cascade;

drop table if exists User_User_Permission cascade;

drop table if exists user_permission cascade;

drop table if exists user_profile cascade;

drop table if exists working_group cascade;

drop table if exists working_group_assembly cascade;

