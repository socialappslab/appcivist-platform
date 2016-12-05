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
  description               text,
  url                       varchar(255),
  listed                    boolean,
  invitationEmail           text,
  profile_assembly_profile_id bigint,
  location_location_id      bigint,
  resources_resource_space_id bigint,
  forum_resource_space_id   bigint,
  creator_user_id           bigint,
  constraint uq_assembly_profile_assembly_pro unique (profile_assembly_profile_id),
  constraint uq_assembly_resources_resource_s unique (resources_resource_space_id),
  constraint uq_assembly_forum_resource_space unique (forum_resource_space_id),
  constraint pk_assembly primary key (assembly_id))
;

create table assembly_profile (
  assembly_profile_id       bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
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
  goal                      text,
  url                       varchar(255),
  uuid                      varchar(40),
  listed                    boolean,
  resources_resource_space_id bigint,
  template_campaign_template_id bigint,
  constraint uq_campaign_resources_resource_s unique (resources_resource_space_id),
  constraint pk_campaign primary key (campaign_id))
;

create table campaign_required_configuration (
  campaign_required_configuration_id bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  campaign_template_campaign_template_id bigint,
  config_definition_uuid    varchar(40),
  constraint pk_campaign_required_configurati primary key (campaign_required_configuration_id))
;

create table campaign_template (
  campaign_template_id      bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  name_key                  varchar(23),
  name                      varchar(255),
  constraint ck_campaign_template_name_key check (name_key in ('PARTICIPATORY_BUDGETING','OCCUPY_ACTION','AWARENESS_RAISING','ACTION_PROMOTION','MOBILIZATION','FUNDRAISING','PROPOSAL_MAKING')),
  constraint pk_campaign_template primary key (campaign_template_id))
;

create table campaign_timeline_edge (
  edge_id                   bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  campaign_campaign_id      bigint,
  start                     boolean,
  from_component_component_id bigint,
  to_component_component_id bigint,
  constraint pk_campaign_timeline_edge primary key (edge_id))
;

create table component (
  component_id              bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  key                       varchar(255),
  description               text,
  start_date                timestamp,
  end_date                  timestamp,
  uuid                      varchar(40),
  position                  integer,
  timeline                  integer,
  definition_component_def_id bigint,
  resource_space_resource_space_id bigint,
  constraint uq_component_resource_space_reso unique (resource_space_resource_space_id),
  constraint pk_component primary key (component_id))
;

create table component_definition (
  component_def_id          bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  name                      varchar(255),
  description               text,
  constraint pk_component_definition primary key (component_def_id))
;

create table component_milestone (
  component_milestone_id    bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  key                       varchar(255),
  position                  integer,
  description               text,
  date                      timestamp,
  days                      integer,
  uuid                      varchar(40),
  type                      varchar(8),
  main_contribution_type    integer,
  constraint ck_component_milestone_type check (type in ('START','END','REMINDER')),
  constraint ck_component_milestone_main_contribution_type check (main_contribution_type in (0,1,2,3,4,5,6,7,8,9,10)),
  constraint pk_component_milestone primary key (component_milestone_id))
;

create table component_required_configuration (
  component_required_configuration_id bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  component_def_component_def_id bigint,
  config_def_uuid           varchar(40),
  constraint pk_component_required_configurat primary key (component_required_configuration_id))
;

create table component_required_milestone (
  component_required_milestone_id bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  description               text,
  key                       varchar(255),
  position                  integer,
  no_duration               boolean,
  type                      varchar(8),
  target_component_uuid     varchar(40),
  campaign_template_campaign_template_id bigint,
  constraint ck_component_required_milestone_type check (type in ('START','END','REMINDER')),
  constraint pk_component_required_milestone primary key (component_required_milestone_id))
;

create table config (
  uuid                      varchar(40) not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  key                       varchar(255),
  value                     text,
  config_target             varchar(13),
  target_uuid               varchar(40),
  definition_uuid           varchar(40),
  constraint ck_config_config_target check (config_target in ('ASSEMBLY','CAMPAIGN','COMPONENT','WORKING_GROUP','MODULE','PROPOSAL','CONTRIBUTION')),
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
  description               text,
  default_value             varchar(255),
  config_target             varchar(13),
  constraint ck_config_definition_config_target check (config_target in ('ASSEMBLY','CAMPAIGN','COMPONENT','WORKING_GROUP','MODULE','PROPOSAL','CONTRIBUTION')),
  constraint uq_config_definition_1 unique (key),
  constraint pk_config_definition primary key (uuid))
;

create table contribution (
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
  location_location_id      bigint,
  budget                    varchar(255),
  resource_space_resource_space_id bigint,
  stats_contribution_statistics_id bigint,
  action_due_date           timestamp,
  action_done               boolean,
  action                    varchar(255),
  assessment_summary        varchar(255),
  extended_text_pad_resource_id bigint,
  constraint ck_contribution_type check (type in (0,1,2,3,4,5,6,7,8,9,10)),
  constraint uq_contribution_location_locatio unique (location_location_id),
  constraint uq_contribution_resource_space_r unique (resource_space_resource_space_id),
  constraint uq_contribution_stats_contributi unique (stats_contribution_statistics_id),
  constraint uq_contribution_extended_text_pa unique (extended_text_pad_resource_id),
  constraint pk_contribution primary key (contribution_id))
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
  shares                    bigint,
  constraint pk_contribution_statistics primary key (contribution_statistics_id))
;

create table contribution_template (
  id                        bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  constraint pk_contribution_template primary key (id))
;

create table contribution_template_section (
  id                        bigserial not null,
  contribution_template_id  bigint not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  title                     varchar(255),
  description               text,
  length                    integer,
  position                  integer,
  constraint pk_contribution_template_section primary key (id))
;

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

create table membership (
  membership_type           varchar(31) not null,
  membership_id             bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  expiration                bigint,
  status                    varchar(9),
  creator_user_id           bigint,
  user_user_id              bigint,
  target_uuid               varchar(40),
  assembly_assembly_id      bigint,
  working_group_group_id    bigint,
  constraint ck_membership_status check (status in ('ACCEPTED','REQUESTED','INVITED','FOLLOWING','REJECTED')),
  constraint pk_membership primary key (membership_id))
;

create table membership_invitation (
  id                        bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  email                     varchar(255),
  user_id                   bigint,
  status                    varchar(9),
  creator_user_id           bigint,
  target_id                 bigint,
  target_type               varchar(8),
  constraint ck_membership_invitation_status check (status in ('ACCEPTED','REQUESTED','INVITED','FOLLOWING','REJECTED')),
  constraint ck_membership_invitation_target_type check (target_type in ('ASSEMBLY','GROUP')),
  constraint pk_membership_invitation primary key (id))
;

create table properties (
  properties_id             bigserial not null,
  key                       varchar(255),
  value                     varchar(255),
  geo_location_id           bigint,
  constraint pk_properties primary key (properties_id))
;

create table resource (
  resource_id               bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  url                       varchar(255),
  resource_type             varchar(7),
  name                      varchar(255),
  pad_id                    varchar(255),
  read_only_pad_id          varchar(255),
  resource_space_with_server_configs varchar(40),
  url_large                 varchar(255),
  url_medium                varchar(255),
  url_thumbnail             varchar(255),
  constraint ck_resource_resource_type check (resource_type in ('PICTURE','VIDEO','PAD','TEXT','WEBPAGE','FILE','AUDIO')),
  constraint pk_resource primary key (resource_id))
;

create table resource_space (
  resource_space_id         bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  type                      varchar(13),
  parent                    varchar(40),
  constraint ck_resource_space_type check (type in ('ASSEMBLY','CAMPAIGN','WORKING_GROUP','COMPONENT','CONTRIBUTION','VOTING_BALLOT')),
  constraint pk_resource_space primary key (resource_space_id))
;

create table s3file (
  id                        varchar(40) not null,
  bucket                    varchar(255),
  name                      varchar(255),
  constraint pk_s3file primary key (id))
;

create table security_role (
  role_id                   bigserial not null,
  name                      varchar(255),
  constraint pk_security_role primary key (role_id))
;

create table theme (
  theme_id                  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  title                     varchar(255),
  description               text,
  icon                      varchar(255),
  cover                     varchar(255),
  constraint pk_theme primary key (theme_id))
;

create table Token_Action (
  token_id                  bigserial not null,
  token                     varchar(255),
  user_id                   bigint,
  membership_invitation_id  bigint,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_Token_Action_type check (type in ('PR','MR','MI','EV')),
  constraint uq_Token_Action_token unique (token),
  constraint uq_Token_Action_membership_invit unique (membership_invitation_id),
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
  address                   text,
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
  text                      text,
  listed                    boolean,
  majority_threshold        varchar(255),
  block_majority            boolean,
  profile_working_group_profile_id bigint,
  invitationEmail           text,
  resources_resource_space_id bigint,
  forum_resource_space_id   bigint,
  constraint uq_working_group_profile_working unique (profile_working_group_profile_id),
  constraint uq_working_group_resources_resou unique (resources_resource_space_id),
  constraint uq_working_group_forum_resource_ unique (forum_resource_space_id),
  constraint pk_working_group primary key (group_id))
;

create table working_group_profile (
  working_group_profile_id  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  supported_membership      varchar(22),
  management_type           varchar(25),
  icon                      varchar(255),
  cover                     varchar(255),
  constraint ck_working_group_profile_supported_membership check (supported_membership in ('OPEN','INVITATION','REQUEST','INVITATION_AND_REQUEST')),
  constraint ck_working_group_profile_management_type check (management_type in ('OPEN','COORDINATED','MODERATED','COORDINATED_AND_MODERATED','DEMOCRATIC')),
  constraint pk_working_group_profile primary key (working_group_profile_id))
;



create table campaign_template_def_components (
  campaign_template_campaign_template_id bigint not null,
  component_definition_component_def_id bigint not null,
  constraint pk_campaign_template_def_components primary key (campaign_template_campaign_template_id, component_definition_component_def_id))
;

create table campaign_template_req_configs (
  campaign_template_campaign_template_id bigint not null,
  campaign_required_configuration_campaign_required_configuration_id bigint not null,
  constraint pk_campaign_template_req_configs primary key (campaign_template_campaign_template_id, campaign_required_configuration_campaign_required_configuration_id))
;

create table contribution_appcivist_user (
  contribution_contribution_id   bigint not null,
  appcivist_user_user_id         bigint not null,
  constraint pk_contribution_appcivist_user primary key (contribution_contribution_id, appcivist_user_user_id))
;

create table contribution_history_appcivist_user (
  contribution_history_contribution_history_id   bigint not null,
  appcivist_user_user_id         bigint not null,
  constraint pk_contribution_history_appcivist_user primary key (contribution_history_contribution_history_id, appcivist_user_user_id))
;

create table MEMBERSHIP_ROLE (
  membership_membership_id       bigint not null,
  role_role_id                   bigint not null,
  constraint pk_MEMBERSHIP_ROLE primary key (membership_membership_id, role_role_id))
;

create table membership_invitation_security_r (
  membership_invitation_id       bigint not null,
  security_role_role_id          bigint not null,
  constraint pk_membership_invitation_security_r primary key (membership_invitation_id, security_role_role_id))
;

create table resource_space_config (
  resource_space_resource_space_id bigint not null,
  config_uuid                    varchar(40) not null,
  constraint pk_resource_space_config primary key (resource_space_resource_space_id, config_uuid))
;

create table resource_space_theme (
  resource_space_resource_space_id bigint not null,
  theme_theme_id                 bigint not null,
  constraint pk_resource_space_theme primary key (resource_space_resource_space_id, theme_theme_id))
;

create table resource_space_campaign (
  resource_space_resource_space_id bigint not null,
  campaign_campaign_id           bigint not null,
  constraint pk_resource_space_campaign primary key (resource_space_resource_space_id, campaign_campaign_id))
;

create table resource_space_campaign_components (
  resource_space_resource_space_id bigint not null,
  component_component_id         bigint not null,
  constraint pk_resource_space_campaign_components primary key (resource_space_resource_space_id, component_component_id))
;

create table resource_space_campaign_milestones (
  resource_space_resource_space_id bigint not null,
  component_milestone_component_milestone_id bigint not null,
  constraint pk_resource_space_campaign_milestones primary key (resource_space_resource_space_id, component_milestone_component_milestone_id))
;

create table resource_space_working_groups (
  resource_space_resource_space_id bigint not null,
  working_group_group_id         bigint not null,
  constraint pk_resource_space_working_groups primary key (resource_space_resource_space_id, working_group_group_id))
;

create table resource_space_contributions (
  resource_space_resource_space_id bigint not null,
  contribution_contribution_id   bigint not null,
  constraint pk_resource_space_contributions primary key (resource_space_resource_space_id, contribution_contribution_id))
;

create table resource_space_contribution_histories (
  resource_space_resource_space_id bigint not null,
  contribution_history_contribution_history_id   bigint not null,
  constraint pk_resource_space_contribution_histories primary key (resource_space_resource_space_id, contribution_history_contribution_history_id))
;

create table resource_space_assemblies (
  resource_space_resource_space_id bigint not null,
  assembly_assembly_id           bigint not null,
  constraint pk_resource_space_assemblies primary key (resource_space_resource_space_id, assembly_assembly_id))
;

create table resource_space_resource (
  resource_space_resource_space_id bigint not null,
  resource_resource_id           bigint not null,
  constraint pk_resource_space_resource primary key (resource_space_resource_space_id, resource_resource_id))
;

create table resource_space_hashtag (
  resource_space_resource_space_id bigint not null,
  hashtag_hashtag_id             bigint not null,
  constraint pk_resource_space_hashtag primary key (resource_space_resource_space_id, hashtag_hashtag_id))
;

create table resource_space_templates (
  resource_space_resource_space_id bigint not null,
  contribution_template_id       bigint not null,
  constraint pk_resource_space_templates primary key (resource_space_resource_space_id, contribution_template_id))
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



create sequence component_seq start with 9000;
create sequence component_definition_seq start with 9000;

alter table assembly add constraint fk_assembly_profile_1 foreign key (profile_assembly_profile_id) references assembly_profile (assembly_profile_id);
create index ix_assembly_profile_1 on assembly (profile_assembly_profile_id);
alter table assembly add constraint fk_assembly_location_2 foreign key (location_location_id) references location (location_id);
create index ix_assembly_location_2 on assembly (location_location_id);
alter table assembly add constraint fk_assembly_resources_3 foreign key (resources_resource_space_id) references resource_space (resource_space_id);
create index ix_assembly_resources_3 on assembly (resources_resource_space_id);
alter table assembly add constraint fk_assembly_forum_4 foreign key (forum_resource_space_id) references resource_space (resource_space_id);
create index ix_assembly_forum_4 on assembly (forum_resource_space_id);
alter table assembly add constraint fk_assembly_creator_5 foreign key (creator_user_id) references appcivist_user (user_id);
create index ix_assembly_creator_5 on assembly (creator_user_id);
alter table campaign add constraint fk_campaign_resources_6 foreign key (resources_resource_space_id) references resource_space (resource_space_id);
create index ix_campaign_resources_6 on campaign (resources_resource_space_id);
alter table campaign add constraint fk_campaign_template_7 foreign key (template_campaign_template_id) references campaign_template (campaign_template_id);
create index ix_campaign_template_7 on campaign (template_campaign_template_id);
alter table campaign_required_configuration add constraint fk_campaign_required_configura_8 foreign key (campaign_template_campaign_template_id) references campaign_template (campaign_template_id);
create index ix_campaign_required_configura_8 on campaign_required_configuration (campaign_template_campaign_template_id);
alter table campaign_required_configuration add constraint fk_campaign_required_configura_9 foreign key (config_definition_uuid) references config_definition (uuid);
create index ix_campaign_required_configura_9 on campaign_required_configuration (config_definition_uuid);
alter table campaign_timeline_edge add constraint fk_campaign_timeline_edge_cam_10 foreign key (campaign_campaign_id) references campaign (campaign_id);
create index ix_campaign_timeline_edge_cam_10 on campaign_timeline_edge (campaign_campaign_id);
alter table campaign_timeline_edge add constraint fk_campaign_timeline_edge_fro_11 foreign key (from_component_component_id) references component (component_id);
create index ix_campaign_timeline_edge_fro_11 on campaign_timeline_edge (from_component_component_id);
alter table campaign_timeline_edge add constraint fk_campaign_timeline_edge_toC_12 foreign key (to_component_component_id) references component (component_id);
create index ix_campaign_timeline_edge_toC_12 on campaign_timeline_edge (to_component_component_id);
alter table component add constraint fk_component_definition_13 foreign key (definition_component_def_id) references component_definition (component_def_id);
create index ix_component_definition_13 on component (definition_component_def_id);
alter table component add constraint fk_component_resourceSpace_14 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);
create index ix_component_resourceSpace_14 on component (resource_space_resource_space_id);
alter table component_required_configuration add constraint fk_component_required_configu_15 foreign key (component_def_component_def_id) references component_definition (component_def_id);
create index ix_component_required_configu_15 on component_required_configuration (component_def_component_def_id);
alter table component_required_configuration add constraint fk_component_required_configu_16 foreign key (config_def_uuid) references config_definition (uuid);
create index ix_component_required_configu_16 on component_required_configuration (config_def_uuid);
alter table component_required_milestone add constraint fk_component_required_milesto_17 foreign key (campaign_template_campaign_template_id) references campaign_template (campaign_template_id);
create index ix_component_required_milesto_17 on component_required_milestone (campaign_template_campaign_template_id);
alter table config add constraint fk_config_definition_18 foreign key (definition_uuid) references config_definition (uuid);
create index ix_config_definition_18 on config (definition_uuid);
alter table contribution add constraint fk_contribution_location_19 foreign key (location_location_id) references location (location_id);
create index ix_contribution_location_19 on contribution (location_location_id);
alter table contribution add constraint fk_contribution_resourceSpace_20 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);
create index ix_contribution_resourceSpace_20 on contribution (resource_space_resource_space_id);
alter table contribution add constraint fk_contribution_stats_21 foreign key (stats_contribution_statistics_id) references contribution_statistics (contribution_statistics_id);
create index ix_contribution_stats_21 on contribution (stats_contribution_statistics_id);
alter table contribution add constraint fk_contribution_extendedTextP_22 foreign key (extended_text_pad_resource_id) references resource (resource_id);
create index ix_contribution_extendedTextP_22 on contribution (extended_text_pad_resource_id);
alter table contribution_template_section add constraint fk_contribution_template_sect_23 foreign key (contribution_template_id) references contribution_template (id);
create index ix_contribution_template_sect_23 on contribution_template_section (contribution_template_id);
alter table geometry add constraint fk_geometry_geo_24 foreign key (geo_location_id) references geo (location_id);
create index ix_geometry_geo_24 on geometry (geo_location_id);
alter table Linked_Account add constraint fk_Linked_Account_user_25 foreign key (user_id) references appcivist_user (user_id);
create index ix_Linked_Account_user_25 on Linked_Account (user_id);
alter table membership add constraint fk_membership_creator_26 foreign key (creator_user_id) references appcivist_user (user_id);
create index ix_membership_creator_26 on membership (creator_user_id);
alter table membership add constraint fk_membership_user_27 foreign key (user_user_id) references appcivist_user (user_id);
create index ix_membership_user_27 on membership (user_user_id);
alter table membership add constraint fk_membership_assembly_28 foreign key (assembly_assembly_id) references assembly (assembly_id);
create index ix_membership_assembly_28 on membership (assembly_assembly_id);
alter table membership add constraint fk_membership_workingGroup_29 foreign key (working_group_group_id) references working_group (group_id);
create index ix_membership_workingGroup_29 on membership (working_group_group_id);
alter table membership_invitation add constraint fk_membership_invitation_crea_30 foreign key (creator_user_id) references appcivist_user (user_id);
create index ix_membership_invitation_crea_30 on membership_invitation (creator_user_id);
alter table properties add constraint fk_properties_geo_31 foreign key (geo_location_id) references geo (location_id);
create index ix_properties_geo_31 on properties (geo_location_id);
alter table Token_Action add constraint fk_Token_Action_targetUser_32 foreign key (user_id) references appcivist_user (user_id);
create index ix_Token_Action_targetUser_32 on Token_Action (user_id);
alter table Token_Action add constraint fk_Token_Action_targetInvitat_33 foreign key (membership_invitation_id) references membership_invitation (id);
create index ix_Token_Action_targetInvitat_33 on Token_Action (membership_invitation_id);
alter table appcivist_user add constraint fk_appcivist_user_profilePic_34 foreign key (profile_pic_resource_id) references resource (resource_id);
create index ix_appcivist_user_profilePic_34 on appcivist_user (profile_pic_resource_id);
alter table user_profile add constraint fk_user_profile_user_35 foreign key (user_user_id) references appcivist_user (user_id);
create index ix_user_profile_user_35 on user_profile (user_user_id);
alter table working_group add constraint fk_working_group_profile_36 foreign key (profile_working_group_profile_id) references working_group_profile (working_group_profile_id);
create index ix_working_group_profile_36 on working_group (profile_working_group_profile_id);
alter table working_group add constraint fk_working_group_resources_37 foreign key (resources_resource_space_id) references resource_space (resource_space_id);
create index ix_working_group_resources_37 on working_group (resources_resource_space_id);
alter table working_group add constraint fk_working_group_forum_38 foreign key (forum_resource_space_id) references resource_space (resource_space_id);
create index ix_working_group_forum_38 on working_group (forum_resource_space_id);

alter table campaign_template_def_components add constraint fk_campaign_template_def_comp_01 foreign key (campaign_template_campaign_template_id) references campaign_template (campaign_template_id);

alter table campaign_template_def_components add constraint fk_campaign_template_def_comp_02 foreign key (component_definition_component_def_id) references component_definition (component_def_id);

alter table campaign_template_req_configs add constraint fk_campaign_template_req_conf_01 foreign key (campaign_template_campaign_template_id) references campaign_template (campaign_template_id);

alter table campaign_template_req_configs add constraint fk_campaign_template_req_conf_02 foreign key (campaign_required_configuration_campaign_required_configuration_id) references campaign_required_configuration (campaign_required_configuration_id);

alter table contribution_appcivist_user add constraint fk_contribution_appcivist_use_01 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table contribution_appcivist_user add constraint fk_contribution_appcivist_use_02 foreign key (appcivist_user_user_id) references appcivist_user (user_id);

alter table contribution_history_appcivist_user add constraint fk_contribution_history_appcivist_use_01 foreign key (contribution_history_contribution_history_id) references contribution_history (contribution_history_id);

alter table contribution_history_appcivist_user add constraint fk_contribution_history_appcivist_use_02 foreign key (appcivist_user_user_id) references appcivist_user (user_id);

alter table MEMBERSHIP_ROLE add constraint fk_MEMBERSHIP_ROLE_membership_01 foreign key (membership_membership_id) references membership (membership_id);

alter table MEMBERSHIP_ROLE add constraint fk_MEMBERSHIP_ROLE_security_r_02 foreign key (role_role_id) references security_role (role_id);

alter table membership_invitation_security_r add constraint fk_membership_invitation_secu_01 foreign key (membership_invitation_id) references membership_invitation (id);

alter table membership_invitation_security_r add constraint fk_membership_invitation_secu_02 foreign key (security_role_role_id) references security_role (role_id);

alter table resource_space_config add constraint fk_resource_space_config_reso_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_config add constraint fk_resource_space_config_conf_02 foreign key (config_uuid) references config (uuid);

alter table resource_space_theme add constraint fk_resource_space_theme_resou_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_theme add constraint fk_resource_space_theme_theme_02 foreign key (theme_theme_id) references theme (theme_id);

alter table resource_space_campaign add constraint fk_resource_space_campaign_re_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_campaign add constraint fk_resource_space_campaign_ca_02 foreign key (campaign_campaign_id) references campaign (campaign_id);

alter table resource_space_campaign_components add constraint fk_resource_space_campaign_co_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_campaign_components add constraint fk_resource_space_campaign_co_02 foreign key (component_component_id) references component (component_id);

alter table resource_space_campaign_milestones add constraint fk_resource_space_campaign_mi_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_campaign_milestones add constraint fk_resource_space_campaign_mi_02 foreign key (component_milestone_component_milestone_id) references component_milestone (component_milestone_id);

alter table resource_space_working_groups add constraint fk_resource_space_working_gro_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_working_groups add constraint fk_resource_space_working_gro_02 foreign key (working_group_group_id) references working_group (group_id);

alter table resource_space_contributions add constraint fk_resource_space_contributio_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_contributions add constraint fk_resource_space_contributio_02 foreign key (contribution_contribution_id) references contribution (contribution_id);

alter table resource_space_contribution_histories add constraint fk_resource_space_contribution_histories_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_contribution_histories add constraint fk_resource_space_contribution_histories_02 foreign key (contribution_history_contribution_history_id) references contribution_history(contribution_history_id);

alter table resource_space_assemblies add constraint fk_resource_space_assemblies__01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_assemblies add constraint fk_resource_space_assemblies__02 foreign key (assembly_assembly_id) references assembly (assembly_id);

alter table resource_space_resource add constraint fk_resource_space_resource_re_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_resource add constraint fk_resource_space_resource_re_02 foreign key (resource_resource_id) references resource (resource_id) 
    MATCH SIMPLE ON UPDATE NO ACTION ON DELETE CASCADE;

alter table resource_space_hashtag add constraint fk_resource_space_hashtag_res_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_hashtag add constraint fk_resource_space_hashtag_has_02 foreign key (hashtag_hashtag_id) references hashtag (hashtag_id);

alter table resource_space_templates add constraint fk_resource_space_templates_r_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_templates add constraint fk_resource_space_templates_c_02 foreign key (contribution_template_id) references contribution_template (id);

alter table User_Security_Roles add constraint fk_User_Security_Roles_appciv_01 foreign key (user_id) references appcivist_user (user_id);

alter table User_Security_Roles add constraint fk_User_Security_Roles_securi_02 foreign key (role_id) references security_role (role_id);

alter table User_User_Permission add constraint fk_User_User_Permission_appci_01 foreign key (user_id) references appcivist_user (user_id);

alter table User_User_Permission add constraint fk_User_User_Permission_user__02 foreign key (permission_id) references user_permission (permission_id);

create index ix_assembly_uuid_39 on assembly(uuid);
create index ix_component_definition_uuid_40 on component_definition(uuid);
create index ix_component_required_milesto_41 on component_required_milestone(target_component_uuid);
create index ix_contribution_uuid_42 on contribution(uuid);
create index ix_contribution_text_index_43 on contribution(text_index);
create index ix_contribution_template_uuid_44 on contribution_template(uuid);
create index ix_contribution_template_sect_45 on contribution_template_section(uuid);
create index ix_location_serialized_locati_46 on location(serialized_location);
create index ix_resource_uuid_47 on resource(uuid);
create index ix_resource_space_uuid_48 on resource_space(uuid);

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

alter table ballot add column require_registration boolean;
alter table ballot add column user_uuid_as_signature boolean;
alter table ballot add column decision_type varchar(40);
alter table campaign add column consultive_ballot varchar(40);
alter table ballot add column component bigint;
alter table campaign add column binding_ballot varchar(40);
alter table working_group add column consensus_ballot varchar(40);

alter table ballot alter column id set default nextval('ballots_id_seq');
alter table ballot_registration_field alter column id set default nextval('ballot_registration_fields_id_seq');
alter table ballot_configuration alter column id set default nextval('ballot_configurations_id_seq');
alter table ballot_paper alter column id set default nextval('ballot_papers_id_seq');
alter table candidate alter column id set default nextval('candidates_id_seq');
alter table vote alter column id set default nextval('votes_id_seq');
 
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

alter table ballot alter column id set default nextval('ballots_id_seq');
alter table ballot_registration_field alter column id set default nextval('ballot_registration_fields_id_seq');
alter table ballot_configuration alter column id set default nextval('ballot_configurations_id_seq');
alter table ballot_paper alter column id set default nextval('ballot_papers_id_seq');
alter table candidate alter column id set default nextval('candidates_id_seq');
alter table vote alter column id set default nextval('votes_id_seq');
 
create table contribution_feedback (
    id                              bigserial not null,
    creation                        timestamp,
    last_update                     timestamp,
    lang                            varchar(255),
    removal                         timestamp,
    removed                         boolean,
    user_id                         bigint not null,
    contribution_id                 bigint not null,
    up                              boolean,
    down                            boolean,
    fav                             boolean,
    flag                            boolean,    
    constraint pk_contribution_feedback primary key (id)
    
);

alter table contribution_feedback add constraint fk_contribution_feedback_user foreign key (user_id) references appcivist_user (user_id);
alter table contribution_feedback add constraint fk_contribution_feedback_contribution foreign key (contribution_id) references contribution (contribution_id);
alter table contribution drop constraint "ck_contribution_type";
alter table contribution add constraint "ck_contribution_type" check (type = ANY (ARRAY[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]));
alter table contribution add column priority INTEGER;

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

--- 10.sql
-- Adding persistence for notifications
create table notification_event_signal (
  id                        bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  origin                    varchar(40),
  origin_type               varchar(13),
  event_name                varchar(40),
  origin_name               varchar(255),
  title                     varchar(255),
  text                      text,
  resource_type             varchar(255), 
  resource_uuid             varchar(40),
  resource_title            varchar(255),
  resource_text             text,
  notification_date         timestamp,  
  associated_user           varchar(255),
  signaled                  boolean,
  constraint pk_notification_event primary key (id))
;

create index ix_notification_event_id on notification_event (id);
create index ix_notification_event_uuid on notification_event (uuid);

create table contribution_publish_history(
  id                        bigserial not null,
  contribution_id           bigint not null,
  resource_id               bigint not null,
  revision                  INTEGER not null,
  creation                  timestamp,
  last_update               timestamp,
  removal                   timestamp,
  removed                   boolean,
  lang                      varchar(255),
  constraint pk_contribution_publish_history primary key (id))
;
create index ix_notification_event_id on notification_event_signal (id);
create index ix_notification_event_uuid on notification_event_signal (uuid);

--- 11.sql
-- Created by @josepmv
-- https://github.com/josepmv/dbadailystuff/blob/master/postgresql_setval_max.sql

-- SETVAL for all sequences in a schema or for a unique table
-- In PostgreSQL, when you’re working with sequences, if you insert a future value due to the incrementing values, you will get an error
--  when that value is going to be inserted. I like much more how SQL Server handles autoincrement columns with its IDENTITY property, 
--  that would be like the sequences linked to a table like SERIAL, but it’s much more restrictive and by default you cannot INSERT a register
--  specifying the value of this column as you can do with PostgreSQL.
-- The PostgreSQL setval() function, explained in Sequence Manipulation Functions (http://www.postgresql.org/docs/current/interactive/functions-sequence.html), 
-- is the way that PostgreSQL has to change the value of a sequence. But only accepts one table as a parameter. 
-- So, if you need to set all the sequences in a schema to the max(id) of every table, 
-- you can do can use the following script, based on Updating sequence values from table select (http://wiki.postgresql.org/wiki/Fixing_Sequences).
-- */

CREATE OR REPLACE FUNCTION setval_max
(
    schema_name name,
    table_name name DEFAULT NULL::name,
    raise_notice boolean DEFAULT false
)
RETURNS void AS
$BODY$

-- Sets all the sequences in the schema "schema_name" to the max(id) of every table (or a specific table, if name is supplied)
-- Examples:
--  SELECT setval_max('public');
--  SELECT setval_max('public','mytable');
--  SELECT setval_max('public',null,true);
--  SELECT setval_max('public','mytable',true);

DECLARE
    row_data RECORD;
    sql_code TEXT;

BEGIN
    IF ((SELECT COUNT(*) FROM pg_namespace WHERE nspname = schema_name) = 0) THEN
        RAISE EXCEPTION 'The schema "%" does not exist', schema_name;
    END IF;

    FOR sql_code IN
        SELECT 'SELECT SETVAL(' ||quote_literal(N.nspname || '.' || S.relname)|| ', MAX(' ||quote_ident(C.attname)|| ') ) FROM ' || quote_ident(N.nspname) || '.' || quote_ident(T.relname)|| ';' AS sql_code
            FROM pg_class AS S
            INNER JOIN pg_depend AS D ON S.oid = D.objid
            INNER JOIN pg_class AS T ON D.refobjid = T.oid
            INNER JOIN pg_attribute AS C ON D.refobjid = C.attrelid AND D.refobjsubid = C.attnum
            INNER JOIN pg_namespace N ON N.oid = S.relnamespace
            WHERE S.relkind = 'S' AND N.nspname = schema_name AND (table_name IS NULL OR T.relname = table_name)
            ORDER BY S.relname
    LOOP
        IF (raise_notice) THEN
            RAISE NOTICE 'sql_code: %', sql_code;
        END IF;
        EXECUTE sql_code;
    END LOOP;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

--- 12.sql
ALTER TABLE theme ADD COLUMN type varchar(255) DEFAULT 'EMERGENT';
ALTER TABLE user_profile ADD COLUMN phone varchar(30);
ALTER TABLE user_profile ADD COLUMN note text;
ALTER TABLE user_profile ADD COLUMN gender varchar(30);

-- 13.sql 
ALTER TABLE component ADD COLUMN type varchar(30) DEFAULT 'IDEAS';

-- 14.sql
ALTER TABLE non_member_author ADD COLUMN gender varchar(30);
ALTER TABLE non_member_author ADD COLUMN age smallint;

