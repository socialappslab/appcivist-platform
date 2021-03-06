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
  comment_count             integer,
  forum_comment_count       integer,
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
  constraint ck_Token_Action_type check (type in ('PR','MR','MI','EV', 'FT')),
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

alter sequence log_id_seq OWNED BY log.id;
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
alter table contribution_feedback add column type character varying (25);
alter table contribution_feedback add column status character varying (25);
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

-- 15.sql
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

alter table contribution add column popularity integer;
alter table contribution add column pinned boolean default false;
alter table log add column remote_address varchar(255);


--18.sql
ALTER TABLE campaign add column forum_resource_space_id integer;

ALTER TABLE campaign
  ADD CONSTRAINT fk_campaign_forum FOREIGN KEY (forum_resource_space_id)
      REFERENCES public.resource_space (resource_space_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;
      
-- 19.sql
alter table log add column remote_host varchar;
alter table log add column comment varchar;
alter table contribution add column forum_resource_space_id integer;

ALTER TABLE contribution
  ADD CONSTRAINT fk_contribution_forum FOREIGN KEY (forum_resource_space_id)
      REFERENCES public.resource_space (resource_space_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

-- 20.sql 
-- Must be super user to create this extension
create extension "uuid-ossp";
CREATE OR REPLACE FUNCTION create_missing_resource_spaces
(
    entity varchar, 
    field varchar, 
    raise_notice boolean DEFAULT false
)
RETURNS void AS
$BODY$
DECLARE
    r RECORD;
    raise_exception BOOLEAN;
    new_resource_space_id BIGINT;
    new_uuid VARCHAR;
    id_field VARCHAR;
    r_id_field BIGINT;
    
BEGIN
    raise_exception = true;
    
    IF ( lower(entity) = 'campaign') THEN   
        IF ( lower(field) = 'resources_resource_space_id') THEN   
            raise_exception = false;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;    
        END IF;
    ELSIF ( lower(entity) = 'contribution') THEN  
        IF ( lower(field) = 'resource_space_resource_space_id') THEN   
            raise_exception = false;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;    
        END IF;
    ELSIF ( lower(entity) = 'assembly') THEN
        IF ( lower(field) = 'resources_resource_space_id') THEN   
            raise_exception = false;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;    
        END IF;
    ELSIF ( lower(entity) = 'working_group') THEN
        IF ( lower(field) = 'resources_resource_space_id') THEN   
            raise_exception = false;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;    
        END IF;
    ELSIF ( lower(entity) = 'component') THEN
        IF ( lower(field) = 'resource_space_resource_space_id') THEN   
            raise_exception = false;    
        ELSIF ( lower(field) = 'forum_resource_space_id') THEN   
            raise_exception = false;    
        END IF;
    END IF;

    IF (raise_exception) THEN 
        RAISE EXCEPTION 'The entity % and field % are not valid', entity, field;
    END IF;

    FOR r IN EXECUTE format('SELECT * FROM %s WHERE %s is null', lower(entity), lower(field))
    LOOP
   
        IF (raise_notice) THEN
            IF ( lower(entity) = 'campaign' OR lower(entity) = 'component' OR lower(entity) = 'contribution' ) THEN   
                RAISE NOTICE 'Creating resource space for %: %', lower(entity),r.title;
            ELSE
                RAISE NOTICE 'Creating resource space for %: %', lower(entity),r.name;
            END IF;
        END IF;
        
        SELECT uuid_generate_v4() into new_uuid;
        
        INSERT INTO resource_space (
            creation, last_update, lang, removed, 
            uuid, type)
        VALUES ( 
            now(), now(), 'en-US', FALSE, 
            new_uuid, upper(entity))
        RETURNING resource_space.resource_space_id INTO new_resource_space_id;
           
        IF ( lower(entity) = 'campaign') THEN   
            -- UPDATE campaign SET resources_resource_space_id = new_resource_space_id WHERE campaign_id = r.campaign_id ;
            id_field = 'campaign_id';
            r_id_field = r.campaign_id;
        ELSIF ( lower(entity) = 'contribution') THEN  
            -- UPDATE contribution SET resource_space_resource_space_id = new_resource_space_id WHERE contribution_id = r.contribution_id ;
            id_field = 'contribution_id';
            r_id_field = r.contribution_id;
        ELSIF ( lower(entity) = 'assembly') THEN
            -- UPDATE assembly SET resources_resource_space_id = new_resource_space_id WHERE assembly_id = r.assembly_id;
            id_field = 'assembly_id';
            r_id_field = r.assembly_id;
        ELSIF ( lower(entity) = 'working_group') THEN
            -- UPDATE working_group SET resources_resource_space_id = new_resource_space_id WHERE working_group_id = r.working_group_id;
            id_field = 'working_group_id';
            r_id_field = r.working_group_id;
        ELSIF ( lower(entity) = 'component') THEN
            -- UPDATE component SET resource_space_resource_space_id = new_resource_space_id WHERE component_id = r.component_id;
            id_field = 'component_id';
            r_id_field = r.component_id;
        END IF;
                
        
        IF (raise_notice) THEN
            RAISE NOTICE 'Updating % by adding new resource space with id %', lower(entity),new_resource_space_id;
            RAISE NOTICE 'UPDATE % SET % = % WHERE % = %',lower(entity), lower(field), new_resource_space_id, id_field, r_id_field;
        END IF;
        EXECUTE format('UPDATE %s SET %s = %L WHERE %s = %L',lower(entity), lower(field), new_resource_space_id, id_field, r_id_field);
    END LOOP;
END;
$BODY$
LANGUAGE plpgsql VOLATILE;

-- 21.sql
alter table assembly add column principalassembly boolean default FALSE;

-- 22.sql
alter table appcivist_user add column creation timestamp;
alter table appcivist_user add column last_update timestamp;
alter table appcivist_user add column lang varchar(255);
alter table appcivist_user add column removal timestamp;
alter table appcivist_user add column removed boolean;

-- 23.sql
-- This evolution is only for keeping evolutions not modified, the full create script already incorporates these changes on creation of the table
alter table contribution add column plain_text text;
update contribution set plain_text = text;

-- 24.sql
CREATE TABLE contribution_history_contribution_feedback
(
  contribution_history_contribution_history_id bigint NOT NULL,
  contribution_feedback_id bigint NOT NULL,
  CONSTRAINT pk_contribution_history_feedback PRIMARY KEY (contribution_history_contribution_history_id, contribution_feedback_id),
  CONSTRAINT fk_contribution_history_feedback_01 FOREIGN KEY (contribution_history_contribution_history_id)
      REFERENCES contribution_history (contribution_history_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_contribution_history_feedback_02 FOREIGN KEY (contribution_feedback_id)
      REFERENCES contribution_feedback (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);

--26.sql
alter table contribution_feedback add column non_member_author_id bigint;
alter table contribution_feedback add constraint fk_non_member_author_feedback foreign key (non_member_author_id) references non_member_author(id);
ALTER TABLE contribution_feedback ALTER COLUMN user_id DROP NOT NULL;

--27.sql
create table organization (
  organization_id                  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  title                     varchar(255),
  description               text,
  logo_resource_id          bigint,
  constraint pk_organization primary key (organization_id),
  constraint uq_organization_resource unique (logo_resource_id))
;

create table resource_space_organization (
  resource_space_resource_space_id             bigint not null,
  organization_organization_id                 bigint not null,
  constraint pk_resource_space_organization primary key (resource_space_resource_space_id, organization_organization_id))
;

alter table resource_space_organization add constraint fk_resource_space_org_resou_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_organization add constraint fk_resource_space_org_theme_02 foreign key (organization_organization_id) references organization (organization_id);

-- 28.sql
alter table contribution add column total_comments integer;
update contribution set total_comments=0;

--29.sql
create table custom_field_definition (
  custom_field_definition_id                  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  name                      varchar(255),
  description               text,
  entity_type               varchar(40),
  entity_filter_attribute_name text,
  entity_filter             text,
  position_field            integer,
  limit_field               text,
  limit_type                varchar(40),
  constraint pk_custom_field_definition primary key (custom_field_definition_id))
;
create table custom_field_value (
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  value                     varchar(255),
  entity_target_type        varchar(40),
  entity_target_uuid        varchar(40));

create table resource_space_custom_field_definition (
  resource_space_resource_space_id                                   bigint not null,
  custom_field_definition_custom_field_definition_id                 bigint not null,
  constraint pk_resource_space_custom_field_definition primary key (resource_space_resource_space_id, custom_field_definition_custom_field_definition_id))
;

alter table resource_space_custom_field_definition add constraint fk_resource_space_org_resou_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_custom_field_definition add constraint fk_resource_space_org_theme_02 foreign key (custom_field_definition_custom_field_definition_id) references custom_field_definition (custom_field_definition_id);

alter table custom_field_value add column custom_field_value_id bigserial not null;

alter table custom_field_value add column custom_field_definition_id bigint;

alter table custom_field_value add constraint pk_custom_field_value primary key (custom_field_value_id);

-- 30.sql
-- Extending field definitions to include also the type of the field value
alter table custom_field_definition add column field_type varchar(40) default 'TEXT';

-- Adding some default values that are useful
ALTER TABLE "public"."appcivist_user" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."assembly" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."ballot" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."ballot_paper" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."campaign" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."candidate" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component_definition" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."config" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."config_definition" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."contribution" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."custom_field_definition" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."custom_field_value" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."notification_event_signal" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."organization" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."resource" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."resource_space" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."s3file" ALTER COLUMN "id" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."token_action" ALTER COLUMN "token" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."user_profile" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."working_group" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();

-- 31.sql
create table resource_space_custom_field_value (
  resource_space_resource_space_id                                                 bigint not null,
  custom_field_value_custom_field_value_id                                         bigint not null,
  constraint pk_resource_space_custom_field_value primary key (resource_space_resource_space_id, custom_field_value_custom_field_value_id))
;

alter table resource_space_custom_field_value add constraint fk_resource_space_custom_value_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_custom_field_value add constraint fk_resource_space_custom_value_02 foreign key (custom_field_value_custom_field_value_id) references custom_field_value (custom_field_value_id);

ALTER TABLE "public"."custom_field_value" ADD CONSTRAINT "fk_custom_field_definition_01" FOREIGN KEY ("custom_field_definition_id") REFERENCES "public"."custom_field_definition"("custom_field_definition_id");

alter table working_group add column is_topic boolean default false;

-- 32.sql
alter table ballot add column entity_type varchar(40);

ALTER TABLE "public"."candidate" RENAME COLUMN "contribution_uuid" TO "candidate_uuid";

alter table resource_space add column consensus_ballot character varying(40);

-- 34.sql
create table resource_space_ballot_history (
  resource_space_resource_space_id                                                 bigint not null,
  ballot_ballot_id                                                                 bigint not null,
  constraint pk_resource_space_ballot_history primary key (resource_space_resource_space_id, ballot_ballot_id))
;

alter table resource_space_ballot_history add constraint fk_resource_space_ballot_history_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_ballot_history add constraint fk_resource_space_ballot_history_02 foreign key (ballot_ballot_id) references ballot (id);

-- 33.sql
CREATE EXTENSION unaccent;

CREATE TEXT SEARCH CONFIGURATION en ( COPY = english );
ALTER TEXT SEARCH CONFIGURATION en ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, english_stem;    

CREATE TEXT SEARCH CONFIGURATION es ( COPY = spanish );
ALTER TEXT SEARCH CONFIGURATION es ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, spanish_stem;

CREATE TEXT SEARCH CONFIGURATION it ( COPY = italian );
ALTER TEXT SEARCH CONFIGURATION it ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, italian_stem;

CREATE TEXT SEARCH CONFIGURATION fr ( COPY = french );
ALTER TEXT SEARCH CONFIGURATION fr ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, french_stem;


ALTER TABLE contribution
ADD COLUMN document tsvector;

UPDATE contribution 
SET document = to_tsvector(contribution.lang::regconfig, unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

CREATE INDEX textsearch_idx ON contribution USING gin(document);

CREATE OR REPLACE FUNCTION contribution_trigger() RETURNS trigger AS $$
begin
  new.document := to_tsvector(new.lang::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));
  return new;
end
$$ LANGUAGE plpgsql;

CREATE TRIGGER tsvectorupdate BEFORE INSERT OR UPDATE
    ON contribution FOR EACH ROW EXECUTE PROCEDURE contribution_trigger();

-- 35.sql
alter table contribution add column cover_resource_id bigint;
alter table contribution add constraint fk_contribution_resource_cover foreign key (cover_resource_id) references resource (resource_id);

-- 36.sql
create table contribution_non_member_author (
  contribution_id                                                 bigint not null,
  non_member_author_id                                            bigint not null,
  constraint pk_contribution_non_member_author primary key (contribution_id, non_member_author_id))
;

alter table contribution_non_member_author add constraint fk_contribution_non_member_author_01 foreign key (contribution_id) references contribution (contribution_id);

alter table contribution_non_member_author add constraint fk_contribution_non_member_author_02 foreign key (non_member_author_id) references non_member_author (id);

insert into contribution_non_member_author select contribution_id, non_member_author_id from contribution where non_member_author_id is not null;

alter table non_member_author add column uuid varchar(40);

ALTER TABLE non_member_author ALTER COLUMN uuid SET DEFAULT uuid_generate_v4();

-- 37.sql
ALTER TABLE contribution
ADD COLUMN document_simple tsvector;

UPDATE contribution
SET document_simple = to_tsvector('simple', unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

CREATE INDEX textsearchwords_idx ON contribution USING gin(document_simple);

CREATE OR REPLACE FUNCTION contribution_trigger() RETURNS trigger AS $$
begin
  new.document := to_tsvector(new.lang::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));
  new.document_simple := to_tsvector('simple', unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));
  return new;
end
$$ LANGUAGE plpgsql;

ALTER TABLE non_member_author ADD COLUMN publishContact BOOLEAN DEFAULT FALSE;
ALTER TABLE non_member_author ADD COLUMN subscribed BOOLEAN DEFAULT FALSE;
ALTER TABLE non_member_author ADD COLUMN Phone varchar(30) DEFAULT '';

-- 38.sql
CREATE TEXT SEARCH CONFIGURATION "en-us" ( COPY = english );
ALTER TEXT SEARCH CONFIGURATION "en-us" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, english_stem;

CREATE TEXT SEARCH CONFIGURATION "es-es" ( COPY = spanish );
ALTER TEXT SEARCH CONFIGURATION "es-es" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, spanish_stem;

CREATE TEXT SEARCH CONFIGURATION "it-it" ( COPY = italian );
ALTER TEXT SEARCH CONFIGURATION "it-it" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, italian_stem;

CREATE TEXT SEARCH CONFIGURATION "fr-fr" ( COPY = french );
ALTER TEXT SEARCH CONFIGURATION "fr-fr" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, french_stem;



alter table non_member_author add column lang varchar(255);
alter table non_member_author add column creation timestamp;
alter table non_member_author add column last_update timestamp;
alter table non_member_author add column removal timestamp;
alter table non_member_author add column removed boolean;

-- 39.sql
-- Execute endpoint first
-- POST       /api/contribution/language

UPDATE contribution
SET document = to_tsvector(contribution.lang::regconfig, unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

-- 40.sql

create table custom_field_value_option (
  custom_field_value_option_id                  bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  name                      varchar(255),
  value                     varchar(255),
  value_type                varchar(40),
  "position"                integer,
  constraint pk_custom_field_value_option primary key (custom_field_value_option_id));

alter table custom_field_value_option add column custom_field_definition_id bigint;

ALTER TABLE custom_field_value_option ADD CONSTRAINT fk_custom_field_value_option_01 FOREIGN KEY ("custom_field_definition_id") REFERENCES custom_field_definition(custom_field_definition_id);
-- 41.sql
alter table campaign add column cover_resource_id bigint;
alter table campaign add constraint fk_contribution_resource_cover foreign key (cover_resource_id) references resource (resource_id);

-- 42.sql
alter table campaign add column logo_resource_id bigint;
alter table campaign add constraint fk_campaign_resource_logo foreign key (logo_resource_id) references resource (resource_id);

-- 43.sql
alter table working_group_profile add column color character varying(255);

-- 44.sql
-- Helper functions to speedup creation of some elements

-- Helper random string
Create or replace function random_string(length integer) returns text as
$$
declare
  chars text[] := '{0,1,2,3,4,5,6,7,8,9,A,B,C,D,E,F,G,H,I,J,K,L,M,N,O,P,Q,R,S,T,U,V,W,X,Y,Z,a,b,c,d,e,f,g,h,i,j,k,l,m,n,o,p,q,r,s,t,u,v,w,x,y,z}';
  result text := '';
  i integer := 0;
begin
  if length < 0 then
    raise exception 'Given length cannot be less than 0';
  end if;
  for i in 1..length loop
    result := result || chars[1+random()*(array_length(chars, 1)-1)];
  end loop;
  return result;
end;
$$ language plpgsql;
ALTER FUNCTION random_string(length integer) OWNER TO appcivist;

ALTER TABLE "public"."campaign" ALTER COLUMN "shortname" SET DEFAULT random_string(8);
ALTER TABLE "public"."campaign" ADD UNIQUE ("shortname");
ALTER TABLE "public"."campaign" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."resource_space" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."component_milestone" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
ALTER TABLE "public"."custom_field_value_option" ALTER COLUMN "uuid" SET DEFAULT uuid_generate_v4();
UPDATE "public"."campaign" SET "shortname" = random_string(8);


-- object: public.component_milestone_component_milestone_id_seq | type: SEQUENCE --
-- DROP SEQUENCE IF EXISTS public.component_milestone_component_milestone_id_seq CASCADE;
CREATE SEQUENCE public.component_component_id_seq
INCREMENT BY 1
MINVALUE 1
MAXVALUE 9223372036854775807
START WITH 1
CACHE 1
NO CYCLE;

ALTER TABLE "public"."component" ALTER COLUMN "component_id" SET DEFAULT nextval('component_component_id_seq'::regclass);

alter sequence component_component_id_seq OWNED BY component.component_id;

-- Create a new campaign under an assembly
CREATE OR REPLACE FUNCTION public.create_new_campaign
  ( assemblyid BIGINT, title VARCHAR,  shortname VARCHAR, goal VARCHAR,  logo TEXT, cover TEXT, lng VARCHAR)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  logo_id BIGINT;
  cover_id BIGINT;
  campaign_id BIGINT;

  logo_uuid VARCHAR;
  cover_uuid VARCHAR;
  campaign_uuid VARCHAR;

  campaign_rs_id BIGINT;
  campaign_forum_rs_id BIGINT;

  assembly_rs_id BIGINT;
  now_date TIMESTAMP;
BEGIN
  SELECT now() into now_date;

  -- logo resource
  INSERT INTO "public"."resource" ("creation", "last_update", "lang", "removed", "url", "resource_type")
  VALUES (now_date, now_date, lng, 'FALSE', logo, 'PICTURE')
  RETURNING "resource_id", "uuid" into logo_id, logo_uuid;
  RAISE NOTICE 'Resource for logo created (id, uuid) = (%,%): ', logo_id, logo_uuid;

  -- cover resource
  INSERT INTO "public"."resource" ("creation", "last_update", "lang", "removed", "url", "resource_type")
  VALUES (now_date, now_date, lng, 'FALSE', cover, 'PICTURE')
  RETURNING "resource_id", "uuid" into cover_id, cover_uuid;
  RAISE NOTICE 'Resource for cover created (id, uuid) = (%,%): ', cover_id, cover_uuid;

  -- campaign resource spaces
  INSERT INTO "public"."resource_space"("creation", "last_update", "lang", "removed", "type")
  VALUES (now_date, now_date, lng, 'FALSE', 'CAMPAIGN')
  RETURNING "resource_space_id" into campaign_rs_id;
  RAISE NOTICE 'Resource Space for campaign created (id) = (%): ', campaign_rs_id;

  INSERT INTO "public"."resource_space"("creation", "last_update", "lang", "removed", "type")
  VALUES (now_date, now_date, lng, 'FALSE', 'CAMPAIGN')
  RETURNING "resource_space_id" into campaign_forum_rs_id;
  RAISE NOTICE 'Public Resource Space for campaign created (id) = (%): ', campaign_forum_rs_id;

  -- create campaign
  INSERT INTO "public"."campaign"
  ("creation", "last_update", "lang", "removed", "title", "shortname", "goal", "listed", "template_campaign_template_id", "cover_resource_id", "logo_resource_id", "resources_resource_space_id", "forum_resource_space_id")
  VALUES
    (now_date, now_date, lng, 'FALSE', title, shortname, goal, 'TRUE', 1, cover_id, logo_id, campaign_rs_id, campaign_forum_rs_id)
  RETURNING "campaign"."campaign_id", "campaign"."uuid" into campaign_id, campaign_uuid;
  RAISE NOTICE 'Campaign created (id, uuid) = (%,%): ', campaign_id, campaign_uuid;

  -- get assembly
  SELECT resources_resource_space_id into assembly_rs_id from assembly where assembly_id = assemblyid;
  RAISE NOTICE 'Adding Campaign to resource space (%) of assembly (%): ', assembly_rs_id, assemblyid;

  -- insert campaign on assembly resource space
  INSERT INTO "public"."resource_space_campaign" (resource_space_resource_space_id, campaign_campaign_id)
  VALUES (assembly_rs_id, campaign_id);

END

$$;
ALTER FUNCTION create_new_campaign( assemblyid BIGINT, title VARCHAR,  shortname VARCHAR, goal VARCHAR,  logo TEXT, cover TEXT, lng VARCHAR) OWNER TO appcivist;

-- Create a new theme and add it to a campaign
CREATE OR REPLACE FUNCTION public.create_theme_and_add_to_campaign (campaign_shortname VARCHAR, theme_title VARCHAR,  theme_description VARCHAR, theme_type VARCHAR, theme_lang VARCHAR)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  new_theme_id BIGINT;
  now_date TIMESTAMP;
  campaign_rs_id BIGINT;
BEGIN
  SELECT now() INTO now_date;

  -- create theme record
  INSERT INTO "public"."theme"
  ("creation", "last_update", "lang", "removed", "title", "description", "type")
  VALUES
    (now_date, now_date, theme_lang, 'FALSE', theme_title, theme_description, theme_type)
  RETURNING "theme_id" INTO new_theme_id;
  RAISE NOTICE 'Theme created (id) = (%): ', new_theme_id;

  -- get campaign resource space id
  SELECT "resources_resource_space_id" INTO campaign_rs_id FROM "public"."campaign" WHERE "campaign"."shortname" = campaign_shortname;
  RAISE NOTICE 'Getting resource space id of campaign %: (id) = (%): ', campaign_shortname, campaign_rs_id;

  -- insert theme into campaign resource space
  INSERT INTO "public"."resource_space_theme" ("resource_space_resource_space_id", "theme_theme_id")
  VALUES (campaign_rs_id, new_theme_id);
  RAISE NOTICE 'Campaign added!';

END
$$;
ALTER FUNCTION create_theme_and_add_to_campaign (campaign_shortname VARCHAR, theme_title VARCHAR,  theme_description VARCHAR, theme_type VARCHAR, theme_lang VARCHAR) OWNER TO appcivist;

-- Read a campaign list of components, ordered by position, and create a timeline graph
CREATE OR REPLACE FUNCTION public.generate_timeline_edges (target_campaign_shortname VARCHAR)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  now_date TIMESTAMP;
  source_component RECORD;
  target_component RECORD;
  target_campaign_rs_id BIGINT;
  target_campaign_id BIGINT;
  target_campaign_lang VARCHAR;
  source_is_start BOOLEAN;
  position_counter INT := 0;

BEGIN
  SELECT now() INTO now_date;

  -- get resource space id of campaign
  SELECT campaign_id, resources_resource_space_id, lang FROM campaign WHERE shortname = target_campaign_shortname
  INTO target_campaign_id, target_campaign_rs_id, target_campaign_lang;

  FOR target_component IN
  SELECT * FROM component c, resource_space_campaign_components rs
  WHERE rs.resource_space_resource_space_id = target_campaign_rs_id
        AND c.component_id = rs.component_component_id
        AND timeline = 1
  ORDER BY position ASC
  LOOP
    -- if target is position start, make it source and go next, put source_is_start to true
    IF (position_counter = 0) THEN
      position_counter := position_counter + 1;
      source_component := target_component;
      RAISE NOTICE 'Starting component => %, %', source_component.component_id, source_component.title;
    ELSE
      RAISE NOTICE 'Creating edge (%) => (%)', source_component.title, target_component.title;

      IF (position_counter =1) THEN
        source_is_start := TRUE;
      ELSE
        source_is_start := FALSE;
      END IF;
      INSERT INTO "public"."campaign_timeline_edge"
      (   "creation", "last_update", "lang", "removed",
          "campaign_campaign_id", "start",
          "from_component_component_id", "to_component_component_id")
      VALUES
        (   now_date, now_date, target_campaign_lang, 'FALSE',
            target_campaign_id, source_is_start,
            source_component.component_id, target_component.component_id);
      position_counter := position_counter + 1;
      source_component := target_component;
    END IF;

  END LOOP;
END

$$;
ALTER FUNCTION generate_timeline_edges(target_campaign_shortname VARCHAR) OWNER TO appcivist;

-- Copy components and milestones from one campaign to the other, shifting dates from a starting point
CREATE OR REPLACE FUNCTION public.copy_components_and_milestones_from_campaign
  (   source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR,
      new_start_date TIMESTAMP
  )
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  now_date TIMESTAMP;
  source_campaign_start_date TIMESTAMP;
  shift_interval INTERVAL;

  source_campaign_rs_id BIGINT;
  target_campaign_rs_id BIGINT;


  new_component RECORD;
  new_milestone RECORD;

  new_component_id BIGINT;
  new_component_rs_id BIGINT;

  new_milestone_id BIGINT;
  new_milestone_rs_id BIGINT;

BEGIN
  SELECT now() INTO now_date;

  -- get resource space id of campaign
  SELECT resources_resource_space_id FROM campaign WHERE shortname = source_campaign_shortname
  INTO source_campaign_rs_id;

  -- get starting date of campaign
  SELECT min(c.start_date) FROM component c, resource_space_campaign_components rs
  WHERE rs.resource_space_resource_space_id = source_campaign_rs_id
        AND c.component_id = rs.component_component_id
  INTO source_campaign_start_date;
  RAISE NOTICE 'Start date of the source campaign = (%): ', source_campaign_start_date;

  SELECT new_start_date - source_campaign_start_date INTO shift_interval;
  RAISE NOTICE 'Shifting interval based on new start date = (%): ', shift_interval;

  -- create component record
  SELECT new_start_date - source_campaign_start_date INTO shift_interval;

  FOR new_component IN
  SELECT * FROM component c, resource_space_campaign_components rs
  WHERE rs.resource_space_resource_space_id = source_campaign_rs_id
        AND c.component_id = rs.component_component_id
  LOOP
    RAISE NOTICE 'Processing component (%,%,%) => %',
    new_component .title, new_component .key, new_component .start_date,
    new_component.start_date + shift_interval;

    -- Create resource space for new component
    INSERT INTO "public"."resource_space"
    ("creation", "last_update", "lang", "removed", "type")
    VALUES
      (now_date, now_date, new_component.lang, 'FALSE', 'COMPONENT')
    RETURNING "resource_space_id" into new_component_rs_id;
    RAISE NOTICE 'Resource Space for component created (id) = (%): ', new_component_rs_id;

    -- Create new component record
    INSERT INTO "public"."component"
    (   "creation", "last_update", "lang", "removed",
        "title", "description", "key", "type",
        "start_date", "end_date",
        "position", "timeline",
        "resource_space_resource_space_id")
    VALUES
      (   now_date, now_date, new_component.lang, 'FALSE',
                    new_component.title, new_component.description, new_component.key, new_component.type,
                    new_component.start_date + shift_interval, new_component.end_date + shift_interval,
                    new_component.position, new_component.timeline,
          new_component_rs_id
      )
    RETURNING "component_id" INTO new_component_id;

    -- read target campaign resource space
    SELECT resources_resource_space_id INTO target_campaign_rs_id
    FROM campaign WHERE shortname = target_campaign_shortname;

    -- Associate component to target campaign
    INSERT INTO "public"."resource_space_campaign_components"
    ("resource_space_resource_space_id", "component_component_id")
    VALUES (target_campaign_rs_id, new_component_id);
    RAISE NOTICE 'Component added!';

    -- Copy milestones on component
    FOR new_milestone IN
    SELECT * FROM component_milestone c, resource_space_campaign_milestones rs
    WHERE rs.resource_space_resource_space_id = new_component.resource_space_resource_space_id
          AND c.component_milestone_id = rs.component_milestone_component_milestone_id
    LOOP
      RAISE NOTICE 'Processing milestone (%,%,%) => %',
      new_milestone.title, new_milestone.key, new_milestone.date,
      new_milestone.date+shift_interval;

      -- Create a copy of the milestone space for new component
      INSERT INTO "public"."component_milestone"
      (   "creation", "last_update", "lang", "removed",
          "title", "description", "key", "position", "type", "main_contribution_type",
          "date", "days")
      VALUES
        (   now_date, now_date, new_milestone.lang, 'FALSE',
                      new_milestone.title, new_milestone.description, new_milestone.key, new_milestone.position, new_milestone.type, new_milestone.main_contribution_type,
                      new_milestone.date+shift_interval, new_milestone.days
        )
      RETURNING "component_milestone_id" into new_milestone_id;

      -- insert milestone on component resource space
      INSERT INTO "public"."resource_space_campaign_milestones"
      ("resource_space_resource_space_id", "component_milestone_component_milestone_id")
      VALUES (new_component_rs_id, new_milestone_id);
      RAISE NOTICE 'New Milestone Added!';
    END LOOP;
  END LOOP;
END

$$;
ALTER FUNCTION copy_components_and_milestones_from_campaign(   source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR, new_start_date TIMESTAMP) OWNER TO appcivist;

-- Copy configs from one campaign to the other
CREATE OR REPLACE FUNCTION public.copy_configs_from_campaign (source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  now_date TIMESTAMP;
  source_campaign_rs_id BIGINT;
  target_campaign_rs_id BIGINT;
  target_campaign_uuid VARCHAR;
  config_to_copy RECORD;
  new_config_uuid VARCHAR;

BEGIN
  SELECT now() INTO now_date;

  -- get resource space id of campaign
  SELECT resources_resource_space_id
  FROM campaign
  WHERE shortname = source_campaign_shortname
  INTO source_campaign_rs_id;

  -- get campaign target uuid
  SELECT resources_resource_space_id, uuid
  FROM campaign
  WHERE shortname = target_campaign_shortname
  INTO target_campaign_rs_id, target_campaign_uuid;

  FOR config_to_copy IN
  SELECT * FROM config c, resource_space_config rs
  WHERE rs.resource_space_resource_space_id = source_campaign_rs_id
        AND c.uuid = rs.config_uuid
  LOOP
    RAISE NOTICE 'Processing config (%,%)',
    config_to_copy.key, config_to_copy.value;
    -- create new config record
    INSERT INTO "public"."config"
    (   "creation", "last_update", "lang", "removed",
        "key", "value", "config_target", "target_uuid", "definition_uuid" )
    VALUES
      (   now_date, now_date, config_to_copy.lang, 'FALSE',
          config_to_copy.key, config_to_copy.value, config_to_copy.config_target, target_campaign_uuid, config_to_copy.definition_uuid)
    RETURNING "uuid" INTO new_config_uuid;


    RAISE NOTICE 'Associatign (config, %) to (campaign, rs id) => (%,%)',
    uuid_generate_v4(), target_campaign_shortname, target_campaign_rs_id;
    -- Associate config to target campaign
    INSERT INTO "public"."resource_space_config"
    ("resource_space_resource_space_id", "config_uuid")
    VALUES (target_campaign_rs_id, new_config_uuid);
    RAISE NOTICE 'Config added!';
  END LOOP;
END

$$;
ALTER FUNCTION copy_configs_from_campaign(source_campaign_shortname VARCHAR, target_campaign_shortname VARCHAR) OWNER TO appcivist;

-- Copy components and milestones from one campaign to the other, shifting dates from a starting point
CREATE OR REPLACE FUNCTION public.create_add_config_to_campaign (config_key VARCHAR, config_value VARCHAR, target_campaign_shortname VARCHAR)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  now_date TIMESTAMP;
  target_campaign_rs_id BIGINT;
  target_campaign_uuid VARCHAR;
  new_config_uuid VARCHAR;
  existing_config RECORD;
  target_campaign_lang VARCHAR;

BEGIN
  SELECT now() INTO now_date;

  -- get campaign target uuid
  SELECT resources_resource_space_id, uuid
  FROM campaign
  WHERE shortname = target_campaign_shortname
  INTO target_campaign_rs_id, target_campaign_uuid, target_campaign_lang;

  RAISE NOTICE 'Checking if config (%) already exists for in RS (%) of campaign (%, %)',
  config_key, target_campaign_rs_id, target_campaign_shortname, target_campaign_uuid;
  SELECT c.* FROM config c, resource_space_config rs
  WHERE rs.resource_space_resource_space_id = target_campaign_rs_id
        AND c.key = config_key
        AND rs.config_uuid = c.uuid
  INTO existing_config;

  RAISE NOTICE  'Existing Config => %', existing_config;


  IF (existing_config.key = config_key) THEN
    -- UPDATE value of config
    RAISE NOTICE 'Config (%) already exists. Updating its value (% => %)', existing_config.key, existing_config.value, config_value;
    UPDATE config SET value = config_value WHERE uuid = existing_config.uuid;
  ELSE
    -- INSERT config and ASSOCIATE to campaign
    RAISE NOTICE 'Processing config (%,%)', config_key, config_value;
    -- create new config record
    INSERT INTO "public"."config"
    (   "creation", "last_update", "lang", "removed",
        "key", "value", "config_target", "target_uuid")
    VALUES
      (   now_date, now_date, target_campaign_lang, 'FALSE',
          config_key, config_value, 'CAMPAIGN', target_campaign_uuid)
    RETURNING "uuid" INTO new_config_uuid;

    RAISE NOTICE 'Associating (config, %) to (campaign, rs id) => (%,%)',
    new_config_uuid, target_campaign_shortname, target_campaign_rs_id;
    -- Associate config to target campaign
    INSERT INTO "public"."resource_space_config"
    ("resource_space_resource_space_id", "config_uuid")
    VALUES (target_campaign_rs_id, new_config_uuid);
    RAISE NOTICE 'Config added!';
  END IF;
END

$$;
ALTER FUNCTION create_add_config_to_campaign (config_key VARCHAR, config_value VARCHAR, target_campaign_shortname VARCHAR) OWNER TO appcivist;

-- Create custom field and add to campaign resource space
CREATE OR REPLACE FUNCTION public.create_add_custom_field_option_value_to_field_definition
  (   target_campaign_shortname VARCHAR, field_name VARCHAR, field_entity_type VARCHAR,
      new_option VARCHAR, new_option_type VARCHAR, new_option_position INTEGER)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
  now_date TIMESTAMP;
  target_campaign_rs_id BIGINT;
  target_field_definition_id BIGINT;
  new_field_value_option_id BIGINT;
  target_lang VARCHAR;

BEGIN
  SELECT now() INTO now_date;
  SELECT resources_resource_space_id INTO target_campaign_rs_id FROM campaign WHERE shortname = target_campaign_shortname;

  SELECT cf.custom_field_definition_id, cf.lang
  INTO target_field_definition_id, target_lang
  FROM resource_space_custom_field_definition rs, custom_field_definition cf
  WHERE rs.resource_space_resource_space_id = target_campaign_rs_id
        AND cf.name = field_name
        AND cf.entity_type = field_entity_type;

  RAISE NOTICE 'Inserting option for field (%,%) on RS (%)', target_field_definition_id, field_name, target_campaign_rs_id;

  INSERT INTO "public"."custom_field_value_option"
  (   "creation", "last_update", "lang", "removed",
      "value", "value_type", "position", "custom_field_definition_id")
  VALUES
    (   now_date, now_date, target_lang, 'FALSE',
        new_option, new_option_type, new_option_position, target_field_definition_id)
  RETURNING "custom_field_value_option_id" INTO new_field_value_option_id;

  --  RAISE NOTICE 'Created option (%) => %', new_option, new_field_value_option_id;

END

$$;
ALTER FUNCTION create_add_custom_field_option_value_to_field_definition ( target_campaign_shortname VARCHAR, field_name VARCHAR, field_entity_type VARCHAR, new_option VARCHAR, new_option_type VARCHAR, new_option_position INTEGER) OWNER TO appcivist;

-- object: public.create_add_custom_field_to_campaign | type: FUNCTION --
-- DROP FUNCTION IF EXISTS public.create_add_custom_field_to_campaign(character varying,character varying,character varying,text,character varying,character varying,character varying,integer,text,character varying,character varying) CASCADE;
CREATE FUNCTION public.create_add_custom_field_to_campaign ( target_campaign_shortname character varying,  new_field_lang character varying,  new_name character varying,  new_description text,  new_entity_type character varying,  new_entity_filter_attribute_name character varying,  new_entity_filter character varying,  new_field_position integer,  new_field_limit text,  new_field_limit_type character varying,  new_field_type character varying)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$


DECLARE
  now_date TIMESTAMP;
  target_campaign_rs_id BIGINT;
  new_custom_field_definition_id BIGINT;

BEGIN
  SELECT now() INTO now_date;
  SELECT resources_resource_space_id INTO target_campaign_rs_id FROM campaign WHERE shortname = target_campaign_shortname;

  INSERT INTO "public"."custom_field_definition"
  (   "creation", "last_update", "lang", "removed",
      "name", "description", "entity_type", "entity_filter_attribute_name",
      "entity_filter", "field_position", "field_limit", "limit_type", "field_type")
  VALUES
    (   now_date, now_date, new_field_lang, 'FALSE',
                  new_name, new_description, new_entity_type, new_entity_filter_attribute_name,
                  new_entity_filter, new_field_position, new_field_limit, new_field_limit_type, new_field_type)
  RETURNING "custom_field_definition_id" INTO new_custom_field_definition_id;

  RAISE NOTICE 'Created custom field (%) => %', new_name, new_custom_field_definition_id;

  INSERT INTO "public"."resource_space_custom_field_definition"
  ("resource_space_resource_space_id", "custom_field_definition_custom_field_definition_id")
  VALUES (target_campaign_rs_id, new_custom_field_definition_id);
  RAISE NOTICE 'Custom Field Added! => (%)', new_custom_field_definition_id;
END


$$;
-- ddl-end --
ALTER FUNCTION public.create_add_custom_field_to_campaign(character varying,character varying,character varying,text,character varying,character varying,character varying,integer,text,character varying,character varying) OWNER TO appcivist;
-- ddl-end --


-- 45.sql
alter table contribution drop constraint ck_contrinution_contrinbutoin_status;

update contribution set status = 'DRAFT' where status = 'NEW';

ALTER TABLE contribution
  ADD CONSTRAINT ck_contrinution_contrinbutoin_status CHECK (status::text = ANY (ARRAY['DRAFT'::character varying, 'PUBLISHED'::character varying,
  'ARCHIVED'::character varying, 'EXCLUDED'::character varying]::text[]));

-- 46.sql
CREATE OR REPLACE FUNCTION lang_contribution(param character varying, OUT result character varying) AS
$$
BEGIN

CASE
   WHEN param = 'es' THEN
      result := 'simple_spanish';
   WHEN param = 'es-ES' THEN
      result := 'simple_spanish';
   WHEN param = 'us' THEN
      result := 'simple_english';
   WHEN param = 'en-US' THEN
      result := 'simple_english';
   WHEN param = 'it' THEN
      result := 'simple_italian';
   WHEN param = 'it-IT' THEN
      result := 'simple_italian';
   WHEN param = 'fr' THEN
      result := 'simple_french';
   WHEN param = 'fr-FR' THEN
      result := 'simple_french';
   ELSE
      result := 'simple_english';
END CASE;

END
$$ LANGUAGE plpgsql;

CREATE TEXT SEARCH DICTIONARY simple_english
   (TEMPLATE = pg_catalog.simple, STOPWORDS = english);

CREATE TEXT SEARCH CONFIGURATION simple_english
   (copy = english);
ALTER TEXT SEARCH CONFIGURATION simple_english
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_english;

CREATE TEXT SEARCH DICTIONARY simple_spanish
   (TEMPLATE = pg_catalog.simple, STOPWORDS = spanish);

CREATE TEXT SEARCH CONFIGURATION simple_spanish
   (copy = spanish);
ALTER TEXT SEARCH CONFIGURATION simple_spanish
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_spanish;

CREATE TEXT SEARCH DICTIONARY simple_french
   (TEMPLATE = pg_catalog.simple, STOPWORDS = french);

CREATE TEXT SEARCH CONFIGURATION simple_french
   (copy = french);
ALTER TEXT SEARCH CONFIGURATION simple_french
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_french;

CREATE TEXT SEARCH DICTIONARY simple_italian
   (TEMPLATE = pg_catalog.simple, STOPWORDS = italian);

CREATE TEXT SEARCH CONFIGURATION simple_italian
   (copy = italian);
ALTER TEXT SEARCH CONFIGURATION simple_italian
   ALTER MAPPING FOR asciihword, asciiword, hword, hword_asciipart, hword_part, word
   WITH simple_italian;

CREATE OR REPLACE FUNCTION contribution_trigger() RETURNS trigger AS $$
begin
  new.document := to_tsvector(new.lang::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));
  new.document_simple := to_tsvector(lang_contribution(new.lang)::regconfig, unaccent(coalesce(new.title,'')) || ' ' || unaccent(coalesce(new.text,'')));
  return new;
end
$$ LANGUAGE plpgsql;

UPDATE contribution
SET document_simple = to_tsvector(lang_contribution(lang)::regconfig, unaccent(coalesce(title,'')) || ' ' || unaccent(coalesce(text,'')));

-- 47.sql
ALTER TABLE location ADD COLUMN additional_info TEXT;
ALTER TABLE location ADD COLUMN best_coordinates integer;
ALTER TABLE location ADD COLUMN source character varying(255) default 'Open Street Map';
ALTER TABLE location ADD COLUMN marked_for_review boolean default false;

CREATE TABLE working_group_location (
  working_group_group_id bigint,
  location_location_id bigint,
  CONSTRAINT pk_working_group_location PRIMARY KEY (working_group_group_id, location_location_id),
  CONSTRAINT fk_group_id FOREIGN KEY (working_group_group_id)
  REFERENCES public.working_group (group_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_location_id FOREIGN KEY (location_location_id)
  REFERENCES public.location (location_id) MATCH SIMPLE
  ON UPDATE NO ACTION ON DELETE NO ACTION
);

update location set marked_for_review = true;

-- 48.sql 
ALTER TABLE "public"."contribution" ALTER COLUMN "source" SET DEFAULT 'AppCivist Online';
UPDATE contribution SET source = 'AppCivist Oline';

-- 49.sql


CREATE SEQUENCE public.subscription_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;


CREATE TABLE public.subscription
(
  id bigint NOT NULL DEFAULT nextval('subscription_id_seq'::regclass),
  user_user_id bigint,
  space_id bigint,
  space_type varchar(255),
  subscription_type varchar(255),
  newsletter_frecuency integer,
  ignored_events jsonb,
  disabled_services jsonb,

  default_service integer,
  default_identity integer,

  CONSTRAINT pk_subscription PRIMARY KEY (id),
  CONSTRAINT fk_user FOREIGN KEY (user_user_id)
      REFERENCES public.appcivist_user (user_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

CREATE UNIQUE INDEX unique_subscription_of_user_in_space ON subscription (user_user_id, space_id, subscription_type);

-- 50. SQL
ALTER TABLE public.config DROP CONSTRAINT ck_config_config_target;
ALTER TABLE public.config ADD CONSTRAINT ck_config_config_target CHECK (config_target::text = ANY (ARRAY['ASSEMBLY'::character varying::text, 'CAMPAIGN'::character varying::text, 'COMPONENT'::character varying::text, 'WORKING_GROUP'::character varying::text, 'MODULE'::character varying::text, 'PROPOSAL'::character varying::text, 'CONTRIBUTION'::character varying::text,'USER'::character varying::text]))


-- 51. SQL
alter table subscription drop column user_user_id;
alter table subscription add column user_id character varying(40);
alter table subscription drop column space_id;
alter table subscription add column space_id character varying(40);

CREATE SEQUENCE public.notification_event_signal_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

DROP TABLE public.notification_event_signal;


CREATE TABLE public.notification_event_signal
(
  id bigint NOT NULL DEFAULT nextval('notification_event_signal_id_seq'::regclass),
  creation timestamp without time zone,
  last_update timestamp without time zone,
  lang character varying(255),
  removal timestamp without time zone,
  removed boolean,
  space_type character varying(255),
  signal_type character varying(255),
  event_id character varying(40),
  text text,
  title character varying(255),
  data jsonb,
  CONSTRAINT pk_notification_event PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.notification_event_signal
  OWNER TO postgres;

-- Index: public.ix_notification_event_id

-- DROP INDEX public.ix_notification_event_id;

CREATE INDEX ix_notification_event_id
  ON public.notification_event_signal
  USING btree
  (id);

-- Index: public.ix_notification_event_uuid

-- 52. SQL

CREATE SEQUENCE public.notification_event_signal_user_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;

-- DROP TABLE public.notification_event_signal_user;

CREATE TABLE public.notification_event_signal_user
(
  id bigint NOT NULL DEFAULT nextval('notification_event_signal_user_id_seq'::regclass),
  creation timestamp without time zone,
  last_update timestamp without time zone,
  lang character varying(255),
  removal timestamp without time zone,
  removed boolean,
  user_user_id bigint,
  signal_id bigint,
  read boolean,

  CONSTRAINT pk_notification_event_signal_user PRIMARY KEY (id),
  CONSTRAINT fk_user FOREIGN KEY (user_user_id) REFERENCES public.appcivist_user (user_id) MATCH SIMPLE,
  CONSTRAINT fk_notification_event_signal FOREIGN KEY (signal_id) REFERENCES public.notification_event_signal (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);

--53.sql
ALTER TABLE campaign ADD COLUMN external_ballot text;
ALTER TABLE campaign RENAME COLUMN binding_ballot TO current_ballot;
ALTER TABLE campaign DROP COLUMN consultive_ballot;

--54.sql
ALTER TABLE ballot ADD COLUMN votes_limit CHARACTER VARYING(40);
ALTER TABLE ballot ADD COLUMN votes_limit_meaning CHARACTER VARYING(15);
ALTER TABLE ballot
  ADD CONSTRAINT ck_ballot_votes_limit_meaning 
  CHECK (votes_limit_meaning::text = 
    ANY (ARRAY['SELECTIONS'::character varying, 'TOKENS'::character varying, 'RANGE'::character varying]));

ALTER TABLE contribution DROP CONSTRAINT ck_contrinution_contrinbutoin_status ;

ALTER TABLE contribution DROP CONSTRAINT ck_contrinution_contrinbutoin_status ;

ALTER TABLE contribution
  ADD CONSTRAINT ck_contrinution_contrinbutoin_status 
  CHECK (status::text = 
    ANY (ARRAY['NEW'::character varying, 'DRAFT'::character varying, 'PUBLISHED'::character varying,
                    'ARCHIVED'::character varying, 'EXCLUDED'::character varying, 'MODERATED'::character varying, 
                    'INBALLOT'::character varying, 'SELECTED'::character varying]::text[]));

-- 55.sql
ALTER TABLE ballot
  DROP CONSTRAINT ck_ballot_status;

ALTER TABLE ballot
  ADD CONSTRAINT ck_ballot_status
  CHECK (status = ANY (ARRAY[0, 2]));

-- 56.sql
ALTER TABLE "public"."campaign" ADD COLUMN "brief" text;

--57.sql

ALTER TABLE resource
  DROP CONSTRAINT ck_resource_resource_type;

ALTER TABLE resource
  ADD CONSTRAINT ck_resource_resource_type CHECK (resource_type::text = ANY (ARRAY['PICTURE'::character varying::text, 'VIDEO'::character varying::text, 'PAD'::character varying::text, 'TEXT'::character varying::text, 'WEBPAGE'::character varying::text, 'FILE'::character varying::text, 'AUDIO'::character varying::text, 'CONTRIBUTION_TEMPLATE'::character varying::text, 'CAMPAIGN_TEMPLATE'::character varying::text, 'PROPOSAL'::character varying::text, 'GDOC'::character varying::text]))


--57.sql
  CREATE TABLE notification_event_signal_archive
(
  id bigint NOT NULL,
  creation timestamp without time zone,
  last_update timestamp without time zone,
  lang character varying(255),
  removal timestamp without time zone,
  removed boolean,
  space_type character varying(255),
  signal_type character varying(255),
  event_id character varying(40),
  text text,
  title character varying(255),
  data jsonb,
  CONSTRAINT pk_notification_event_archive PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE notification_event_signal
  OWNER TO postgres;

--58.sql
CREATE TABLE notification_event_signal_user_archival
(
  id bigint NOT NULL,
  creation timestamp without time zone,
  last_update timestamp without time zone,
  lang character varying(255),
  removal timestamp without time zone,
  removed boolean,
  user_user_id bigint,
  signal_id bigint,
  read boolean,
  CONSTRAINT pk_notification_event_signal_archive_user PRIMARY KEY (id),
  CONSTRAINT fk_notification_event_signal_archive FOREIGN KEY (signal_id)
      REFERENCES notification_event_signal_archive (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_user FOREIGN KEY (user_user_id)
      REFERENCES appcivist_user (user_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE notification_event_signal_user
  OWNER TO postgres;

ALTER TABLE "public"."appcivist_user"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."assembly"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."assembly_profile"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."ballot"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."ballot_configuration"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."ballot_paper"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."campaign"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."campaign_required_configuration"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."campaign_template"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."campaign_timeline_edge"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."candidate"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."component"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_definition"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_milestone"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_required_configuration"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."component_required_milestone"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."config"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."config_definition"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_feedback"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_history"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_publish_history"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_statistics"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_template"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."contribution_template_section"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."custom_field_definition"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."custom_field_value"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."custom_field_value_option"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."geo"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."hashtag"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."membership"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."membership_invitation"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."non_member_author"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."notification_event_signal"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."notification_event_signal_archive"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."notification_event_signal_user"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."organization"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."resource"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."resource_space"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."theme"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."token_action" ALTER COLUMN "created" SET DEFAULT now();
ALTER TABLE "public"."user_profile"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."vote"
  ALTER COLUMN "created_at" SET DEFAULT now(),
  ALTER COLUMN "updated_at" SET DEFAULT now();
ALTER TABLE "public"."working_group"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();
ALTER TABLE "public"."working_group_profile"
  ALTER COLUMN "creation" SET DEFAULT now(),
  ALTER COLUMN "last_update" SET DEFAULT now();

-- 58.sql
ALTER TABLE public.working_group ADD COLUMN creator_user_id bigint;
ALTER TABLE public.working_group ADD CONSTRAINT fk_creator_wk FOREIGN KEY (creator_user_id) REFERENCES public.appcivist_user (user_id) MATCH SIMPLE;
ALTER TABLE public.campaign ADD COLUMN creator_user_id bigint;
ALTER TABLE public.campaign ADD CONSTRAINT fk_creator_campaign FOREIGN KEY (creator_user_id) REFERENCES public.appcivist_user (user_id) MATCH SIMPLE;

-- 59.sql
--59.sql
CREATE OR REPLACE FUNCTION move_signals(init_date timestamp)
RETURNS void AS
$BODY$
begin
	case when (select count(*) FROM public.notification_event_signal where creation < init_date) > 0 then
		WITH tmp_event AS (DELETE FROM public.notification_event_signal where creation < init_date RETURNING *)
		INSERT INTO public.notification_event_signal_archive SELECT * FROM tmp_event;
	when (select count(*) FROM public.notification_event_signal_user where signal_id in
	(SELECT id from public.notification_event_signal where creation < init_date ) > 0) THEN
		WITH tmp_user AS (DELETE FROM public.notification_event_signal_user where
		signal_id in (SELECT id from public.notification_event_signal where creation < init_date )
		RETURNING *)
		INSERT INTO public.notification_event_signal_user_archive SELECT * FROM tmp_user;
	end case;
	exception when others then
	   RAISE EXCEPTION 'ERROR. %', SQLERRM
	   USING ERRCODE = 'ER001';
end;
$BODY$
LANGUAGE plpgsql VOLATILE

ALTER TABLE campaign ADD COLUMN status character varying(40);
ALTER TABLE working_group ADD COLUMN status character varying(40);

ALTER TABLE assembly ADD COLUMN status character varying(40);

ALTER TABLE assembly ADD COLUMN status character varying(40);

-- 60.sql
ALTER TABLE notification_event_signal ADD COLUMN rich_text text;

-- 61.sql
ALTER TABLE "public"."resource" ADD COLUMN "resource_auth_key" text;
ALTER TABLE "public"."contribution" ADD COLUMN "extended_text_pad_resource_number" integer DEFAULT 1;
ALTER TABLE "public"."resource" ADD COLUMN "is_template" boolean DEFAULT false;
ALTER TABLE "public"."s3file" ADD COLUMN "creation" timestamp DEFAULT now();
ALTER TABLE "public"."subscription" ADD COLUMN "creation" timestamp DEFAULT now();
ALTER TABLE "public"."resource_space_association_history" ALTER COLUMN "creation" SET DEFAULT now();
-- 62.sql
CREATE EXTENSION pgagent;
CREATE LANGUAGE plpgsql;
DO $$
DECLARE
    jid integer;
    scid integer;
BEGIN
-- Creating a new job
INSERT INTO pgagent.pga_job(
    jobjclid, jobname, jobdesc, jobhostagent, jobenabled
) VALUES (
    1::integer, 'Notification Archival Job'::text, ''::text, ''::text, true
) RETURNING jobid INTO jid;

-- Steps
-- Inserting a step (jobid: NULL)
INSERT INTO pgagent.pga_jobstep (
    jstjobid, jstname, jstenabled, jstkind,
    jstconnstr, jstdbname, jstonerror,
    jstcode, jstdesc
) VALUES (
    jid, 'Notification Arhival Job'::text, true, 's'::character(1),
    ''::text, 'appcivistcore'::name, 'f'::character(1),
    'select * from move_signals((select ''now''::timestamp - ''1 month''::interval));'::text, ''::text
) ;

-- Schedules
-- Inserting a schedule
INSERT INTO pgagent.pga_schedule(
    jscjobid, jscname, jscdesc, jscenabled,
    jscstart, jscend,    jscminutes, jschours, jscweekdays, jscmonthdays, jscmonths
) VALUES (
    jid, 'Notification Schedule'::text, ''::text, true,
    '2017-12-04 10:18:28-03'::timestamp with time zone, '2030-12-18 10:18:13-03'::timestamp with time zone,
    -- Minutes
    ARRAY[false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false]::boolean[],
    -- Hours
    ARRAY[true, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false]::boolean[],
    -- Week days
    ARRAY[true, false, false, false, false, false, false]::boolean[],
    -- Month days
    ARRAY[false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false]::boolean[],
    -- Months
    ARRAY[false, false, false, false, false, false, false, false, false, false, false, false]::boolean[]
) RETURNING jscid INTO scid;
END;

--63.sql
ALTER TABLE "public"."subscription" DROP COLUMN default_identity;
ALTER TABLE "public"."subscription" ADD COLUMN default_identity text;

--64.sql
ALTER TABLE "public"."campaign" ADD COLUMN location_location_id bigint;
ALTER TABLE "public"."campaign" ADD CONSTRAINT fk_campaign_location FOREIGN KEY (location_location_id)
      REFERENCES location (location_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION;
CREATE INDEX ix_campaign_location
  ON campaign
  USING btree
  (location_location_id);
  
--65.sql
ALTER TABLE "public"."component_milestone" ADD COLUMN end_date timestamp without time zone;

--66.sql
CREATE TABLE contribution_status_audit
(
  id bigserial NOT NULL,
  status_start_date timestamp without time zone not null,
  status_end_date timestamp without time zone,
  contribution_contribution_id bigint NOT NULL,
  status character varying(25),
  CONSTRAINT pk_contribution_contribution_status_audit PRIMARY KEY (id),
  CONSTRAINT fk_contribution_status_audit_contribution FOREIGN KEY (contribution_contribution_id)
      REFERENCES contribution (contribution_id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT ck_contribution_status_audit_status CHECK (status::text = ANY (ARRAY['NEW'::character varying, 'PUBLISHED'::character varying,
  'ARCHIVED'::character varying, 'EXCLUDED'::character varying, 'DRAFT'::character varying, 'INBALLOT'::character varying]::text[]))
);

-- 67.sql
CREATE OR REPLACE FUNCTION create_contribution_audits() RETURNS void AS
$BODY$
DECLARE
  r contribution%rowtype;
  cSid bigint;
  campaignId bigint;
  currentComponentId bigint;
  previousComponentId bigint;
  nowDate TIMESTAMP;
  previousComponentEndDate TIMESTAMP;
  previousComponentStartDate TIMESTAMP;
BEGIN
  SELECT now() INTO nowDate;
  FOR r IN
  SELECT c.*
  FROM contribution c, resource_space_contributions rsc, resource_space rs
  WHERE rsc.resource_space_resource_space_id = rs.resource_space_id
        AND rs.type = 'CAMPAIGN'
        AND rsc.contribution_contribution_id = c.contribution_id

  LOOP
    -- 1. Get resource space of the campaign where the contribution was posted
    SELECT rsc.resource_space_resource_space_id
    FROM resource_space_contributions rsc, resource_space rs
    WHERE
      rsc.resource_space_resource_space_id = rs.resource_space_id
      and rs.type = 'CAMPAIGN'
      and rsc.contribution_contribution_id = r.contribution_id
    LIMIT 1
    INTO cSid;

    -- 2. Get the campaign ID related to the resource space
    SELECT c.campaign_id
    FROM campaign c
    WHERE c.resources_resource_space_id = cSid
    INTO campaignId;

    -- 3. Get the component that is current in the campaign timeline
    SELECT co.component_id
    FROM component co, campaign_timeline_edge cte
    WHERE
      co.start_date < nowDate
      and co.end_date > nowDate
      and cte.campaign_campaign_id = campaignId
      and co.component_id = cte.to_component_component_id
    INTO currentComponentId;


    -- 4. Get the component that is previous in the campaign timeline
    SELECT cte.from_component_component_id
    FROM campaign_timeline_edge cte
    WHERE
      cte.to_component_component_id = currentComponentId
      AND cte.campaign_campaign_id = campaignId
    INTO previousComponentId;

    -- 5. Get start and end date of previous component
    SELECT start_date FROM component WHERE component_id = previousComponentId INTO previousComponentStartDate;
    SELECT end_date FROM component WHERE component_id = previousComponentId INTO previousComponentEndDate;

    RAISE NOTICE 'Processing (cont, stat, camp, currComp, prevComp, prevStart, prevEnd => %, %, %, %, %, %, %', r.contribution_id, r.status, campaignId, currentComponentId, previousComponentId, previousComponentStartDate, previousComponentEndDate;
    IF r.status = 'PUBLISHED' AND previousComponentId > 0 THEN
      -- 7. Insert current status, not ended yet
      INSERT INTO public.contribution_status_audit(
        status_start_date, status_end_date, contribution_contribution_id, status)
      VALUES (r.creation, previousComponentEndDate, r.contribution_id, 'DRAFT');
    END IF;


    IF r.status = 'EXCLUDED' AND previousComponentId > 0 THEN
      RAISE NOTICE 'Auditing EXCLUDED contribution => %, %', r.contribution_id, r.title;
      RAISE NOTICE 'Inserting DRAFT audit (creation, previous component end date) => %, %', r.creation, previousComponentStartDate;
      -- 7. Insert current status, not ended yet
      INSERT INTO public.contribution_status_audit(
        status_start_date, status_end_date, contribution_contribution_id, status)
      VALUES (r.creation, previousComponentStartDate, r.contribution_id, 'DRAFT');

      RAISE NOTICE 'Inserting PUBLISHED audit (creation, previous component end date) => %, %', previousComponentStartDate, previousComponentEndDate;
      -- 8. Insert current status, not ended yet
      INSERT INTO public.contribution_status_audit(
        status_start_date, status_end_date, contribution_contribution_id, status)
      VALUES (previousComponentStartDate, previousComponentEndDate, r.contribution_id, 'PUBLISHED');
    END IF;

  END LOOP;
  RETURN;
END
$BODY$
LANGUAGE plpgsql;

select create_contribution_audits();

--68.sql
ALTER TABLE resource
DROP CONSTRAINT ck_resource_resource_type;
ALTER TABLE resource
ADD CONSTRAINT ck_resource_resource_type CHECK (resource_type::text = ANY (ARRAY['PICTURE'::character varying::text, 'VIDEO'::character varying::text, 'PAD'::character varying::text, 'TEXT'::character varying::text, 'WEBPAGE'::character varying::text, 'FILE'::character varying::text, 'AUDIO'::character varying::text, 'CONTRIBUTION_TEMPLATE'::character varying::text, 'CAMPAIGN_TEMPLATE'::character varying::text, 'PROPOSAL'::character varying::text, 'GDOC'::character varying::text, 'PEERDOC'::character varying::text]));

--69.sql
ALTER TABLE "public"."organization" ADD COLUMN "url" text;

-- 70.sql
CREATE TEXT SEARCH CONFIGURATION "pt-br" ( COPY = portuguese );
ALTER TEXT SEARCH CONFIGURATION "pt-br" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, portuguese_stem;

CREATE TEXT SEARCH CONFIGURATION "fr-ca" ( COPY = french );
ALTER TEXT SEARCH CONFIGURATION "fr-ca" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, french_stem;

CREATE TEXT SEARCH CONFIGURATION "de-de" ( COPY = german );
ALTER TEXT SEARCH CONFIGURATION "de-de" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, german_stem;

CREATE TEXT SEARCH CONFIGURATION "pt" ( COPY = portuguese );
ALTER TEXT SEARCH CONFIGURATION "pt" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, portuguese_stem;

CREATE TEXT SEARCH CONFIGURATION "de" ( COPY = german );
ALTER TEXT SEARCH CONFIGURATION "de" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, german_stem;

--71.sql
ALTER TABLE contribution DROP CONSTRAINT ck_contribution_contribution_status;
ALTER TABLE contribution ADD CONSTRAINT ck_contribution_contribution_status CHECK (status::text = ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text, 'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text, 'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text]));
-- 72.sql
ALTER TABLE "public"."custom_field_definition" ADD COLUMN "entity_part" text;

-- 73.sql
ALTER TABLE "public"."component" ADD COLUMN "instructions" text;


-- 74.sql
-- Add or update configs in an assenbly
CREATE OR REPLACE FUNCTION public.create_add_config_to_assembly (config_key VARCHAR, config_value VARCHAR, target_assembly_shortname VARCHAR)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$

DECLARE
now_date TIMESTAMP;
target_assembly_rs_id BIGINT;
target_assembly_uuid VARCHAR;
new_config_uuid VARCHAR;
existing_config RECORD;
target_assembly_lang VARCHAR;

BEGIN
SELECT now() INTO now_date;

-- get assembly target uuid
SELECT resources_resource_space_id, uuid
FROM assembly
WHERE shortname = target_assembly_shortname
INTO target_assembly_rs_id, target_assembly_uuid, target_assembly_lang;

RAISE NOTICE 'Checking if config (%) already exists for in RS (%) of assembly (%, %)',
config_key, target_assembly_rs_id, target_assembly_shortname, target_assembly_uuid;
SELECT c.* FROM config c, resource_space_config rs
WHERE rs.resource_space_resource_space_id = target_assembly_rs_id
      AND c.key = config_key
      AND rs.config_uuid = c.uuid
INTO existing_config;

RAISE NOTICE  'Existing Config => %', existing_config;


IF (existing_config.key = config_key) THEN
-- UPDATE value of config
RAISE NOTICE 'Config (%) already exists. Updating its value (% => %)', existing_config.key, existing_config.value, config_value;
UPDATE config SET value = config_value WHERE uuid = existing_config.uuid;
ELSE
-- INSERT config and ASSOCIATE to assembly
RAISE NOTICE 'Processing config (%,%)', config_key, config_value;
-- create new config record
INSERT INTO "public"."config"
(   "creation", "last_update", "lang", "removed",
    "key", "value", "config_target", "target_uuid")
VALUES
  (   now_date, now_date, target_assembly_lang, 'FALSE',
      config_key, config_value, 'ASSEMBLY', target_assembly_uuid)
RETURNING "uuid" INTO new_config_uuid;

RAISE NOTICE 'Associating (config, %) to (assembly, rs id) => (%,%)',
new_config_uuid, target_assembly_shortname, target_assembly_rs_id;
-- Associate config to target assembly
INSERT INTO "public"."resource_space_config"
("resource_space_resource_space_id", "config_uuid")
VALUES (target_assembly_rs_id, new_config_uuid);
RAISE NOTICE 'Config added!';
END IF;
END

$$;
ALTER FUNCTION create_add_config_to_assembly (config_key VARCHAR, config_value VARCHAR, target_assembly_shortname VARCHAR) OWNER TO appcivist;

-- 75.sql
CREATE OR REPLACE FUNCTION public.create_add_custom_field_to_campaign ( target_campaign_shortname character varying,  new_field_lang character varying,  new_name character varying,  new_description text,  new_entity_type character varying,  new_entity_filter_attribute_name character varying,  new_entity_filter character varying,  new_field_position integer,  new_field_limit text,  new_field_limit_type character varying,  new_field_type character varying, new_entity_part text)
  RETURNS void
LANGUAGE plpgsql
VOLATILE
CALLED ON NULL INPUT
SECURITY INVOKER
COST 100
AS $$


DECLARE
now_date TIMESTAMP;
target_campaign_rs_id BIGINT;
new_custom_field_definition_id BIGINT;

BEGIN
SELECT now() INTO now_date;
SELECT resources_resource_space_id INTO target_campaign_rs_id FROM campaign WHERE shortname = target_campaign_shortname;

INSERT INTO "public"."custom_field_definition"
(   "creation", "last_update", "lang", "removed",
    "name", "description", "entity_type", "entity_filter_attribute_name",
    "entity_filter", "field_position", "field_limit", "limit_type", "field_type", "entity_part")
VALUES
  (   now_date, now_date, new_field_lang, 'FALSE',
                new_name, new_description, new_entity_type, new_entity_filter_attribute_name,
                new_entity_filter, new_field_position, new_field_limit, new_field_limit_type,
      new_field_type, new_entity_part)
RETURNING "custom_field_definition_id" INTO new_custom_field_definition_id;

RAISE NOTICE 'Created custom field (%) => %', new_name, new_custom_field_definition_id;

INSERT INTO "public"."resource_space_custom_field_definition"
("resource_space_resource_space_id", "custom_field_definition_custom_field_definition_id")
VALUES (target_campaign_rs_id, new_custom_field_definition_id);
RAISE NOTICE 'Custom Field Added! => (%)', new_custom_field_definition_id;
END


$$;
-- ddl-end --
ALTER FUNCTION public.create_add_custom_field_to_campaign(character varying,character varying,character varying,text,character varying,character varying,character varying,integer,text,character varying,character varying,text) OWNER TO appcivist;
-- ddl-end --

ALTER TABLE "public"."theme" ADD COLUMN "url" text;

-- 76.sql
create table campaign_participation (
    user_user_id             bigint,
    campaign_campaign_id     bigint,
    creation_date            timestamp default now(),
    user_consent             boolean,
    user_provided_consent    boolean,
    constraint pk_campaign_participation primary key (user_user_id, campaign_campaign_id),
    constraint fk_campaign_participation_user foreign key (user_user_id) references appcivist_user (user_id),
    constraint fk_campaign_participation_campaign foreign key (campaign_campaign_id) references campaign (campaign_id)
);

--77.sql
alter table campaign_participation drop CONSTRAINT pk_campaign_participation;
alter TABLE campaign_participation add COLUMN campaign_participation_id bigserial;
UPDATE campaign_participation SET campaign_participation_id=nextval('campaign_participation_campaign_participation_id_seq');
alter TABLE campaign_participation add CONSTRAINT pk_campaign_participation primary key (campaign_participation_id);

-- 78.sql
UPDATE public.contribution SET comment_count = 0 WHERE comment_count < 0;

-- 79.sql
CREATE TEXT SEARCH CONFIGURATION "es-py" ( COPY = spanish );
ALTER TEXT SEARCH CONFIGURATION "es-py" ALTER MAPPING
FOR hword, hword_part, word WITH unaccent, spanish_stem;

-- 80.sql
alter table contribution_non_member_author drop constraint fk_contribution_non_member_author_02;
alter table contribution_non_member_author   add CONSTRAINT fk_contribution_non_member_author_02 FOREIGN KEY (non_member_author_id)
      REFERENCES public.non_member_author (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

-- 81.sql
ALTER TABLE contribution ADD column creator_user_id bigint;
ALTER TABLE contribution ADD constraint fk_contribution_user foreign key (creator_user_id) references appcivist_user (user_id);
UPDATE contribution set creator_user_id = (select appcivist_user_user_id from contribution_appcivist_user where contribution_contribution_id = contribution_id limit 1);

-- 82.sql
CREATE OR REPLACE FUNCTION update_duplicate_users()
  RETURNS void AS
$BODY$
DECLARE
    r appcivist_user%rowtype;
    oldest appcivist_user%rowtype;
BEGIN
    FOR r IN
        (select * from appcivist_user ou
	join linked_account la on la.user_id = ou.user_id
	where (select count(*) from appcivist_user inr
	where inr.email = ou.email) > 1 order by email, ou.user_id)
    LOOP
        IF oldest IS NULL or (r.email <> oldest.email) THEN
            oldest = r;
            raise notice 'A mantener: %', oldest.user_id;
        else
	   raise notice 'A actualizar: %', r.user_id;
	   update linked_account set user_id = oldest.user_id where user_id = r.user_id;
	   update membership set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update contribution set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update contribution_appcivist_user set appcivist_user_user_id = oldest.user_id
		where appcivist_user_user_id = r.user_id and (select count(*) from contribution_appcivist_user where
		appcivist_user_user_id = oldest.user_id ) = 0;
	   update assembly set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update campaign set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update campaign_participation set user_user_id = oldest.user_id where user_user_id = r.user_id;
	   update membership_invitation set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update notification_event_signal_user set user_user_id = oldest.user_id where user_user_id = r.user_id;
	   update subscription set user_id = oldest.uuid where user_id = r.uuid;
	   update working_group set creator_user_id = oldest.user_id where creator_user_id = r.user_id;
	   update appcivist_user set removed = true, email = r.email || '.invalid' where user_id = r.user_id;

        end if;

    END LOOP;
    RETURN;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER FUNCTION update_duplicate_users()
  OWNER TO postgres;

select * from update_duplicate_users();

-- 83.sql
CREATE OR REPLACE FUNCTION update_duplicate_themes()
  RETURNS void AS
$BODY$
DECLARE
    r theme%rowtype;
    oldest theme%rowtype;
BEGIN
    FOR r IN
        (select * from theme ou
	where (select count(*) from theme inr
	where inr.title = ou.title and inr.type = 'EMERGENT') > 1 AND type = 'EMERGENT' order by title, creation)
    LOOP
        IF oldest IS NULL or (r.title <> oldest.title) THEN
            oldest = r;
            raise notice 'A mantener: %', oldest.theme_id;
        else
	   raise notice 'A actualizar: %', r.theme_id;
	   update resource_space_theme set theme_theme_id = oldest.theme_id where theme_theme_id = r.theme_id  and (select count(*) from theme where
		theme_id = oldest.theme_id ) = 0;
	   delete from resource_space_theme where theme_theme_id = r.theme_id;
	   delete from theme where theme_id = r.theme_id;
     
        end if;

    END LOOP;
    RETURN;
END
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;

ALTER FUNCTION update_duplicate_themes()
  OWNER TO postgres;

select * from update_duplicate_themes();

-- 84.sql
alter TABLE appcivist_user add COLUMN email_updated boolean;

--85.sql

ALTER TABLE s3file
RENAME TO appcivist_file;
alter TABLE appcivist_file add COLUMN target character varying(10) ;
alter TABLE appcivist_file add COLUMN url character varying(255) ;

-- 86.sql
ALTER TABLE contribution_status_audit DROP CONSTRAINT ck_contribution_status_audit_status;
ALTER TABLE contribution_status_audit ADD CONSTRAINT ck_contribution_status_audit_status CHECK (status::text = ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text, 'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text, 'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text]));

-- 87.sql

insert into contribution_appcivist_user (contribution_contribution_id, appcivist_user_user_id)
select c.contribution_id, b.user_id from non_member_author a join appcivist_user b on a.email = b.email
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user;

delete  from contribution_non_member_author a where (non_member_author_id, contribution_id)
in
                                                   (select c.non_member_author_id, c.contribution_id from non_member_author a join appcivist_user b on a.email = b.email
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user);


insert into contribution_appcivist_user (contribution_contribution_id, appcivist_user_user_id)
select c.contribution_id, b.user_id from non_member_author a join appcivist_user b on a.name = b.name and a.email is null
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user;

delete  from contribution_non_member_author a where (non_member_author_id, contribution_id)
in
                                                   (select c.non_member_author_id, c.contribution_id from non_member_author a join appcivist_user b on a.name = b.name and a.email is null
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user);

-- 88.sql

CREATE OR REPLACE FUNCTION add_proposals_from_theme_to_wg(theme_id bigint, wg_id bigint) RETURNS void AS $$
     DECLARE

     BEGIN
       insert into resource_space_working_groups
         select resource_space_resource_space_id, wg_id from contribution where resource_space_resource_space_id in (
         select distinct resource_space_resource_space_id
         from resource_space_theme
         where theme_theme_id = theme_id
       ) and type = 4 and
            NOT EXISTS ( select rs.working_group_group_id from
       resource_space_working_groups rs where rs.resource_space_resource_space_id = resource_space_resource_space_id
            and rs.working_group_group_id = wg_id);
     END;
 $$ LANGUAGE plpgsql;
-- 89.sql


CREATE OR REPLACE FUNCTION change_status_contribution_in_campaign(search_shortname character, date timestamp, target character) RETURNS void AS $$
    BEGIN
      update contribution set status = target where contribution_id in (
      select contribution_id from contribution where contribution_id in (
      select contribution_contribution_id from resource_space_contributions where resource_space_resource_space_id in (
      SELECT c.resources_resource_space_id FROM campaign c WHERE c.shortname = search_shortname) and creation < date::timestamp));
    END;
    $$ LANGUAGE plpgsql;

-- 90.sql
ALTER TABLE contribution ADD column parent_id bigint;
ALTER TABLE contribution ADD constraint fk_contribution_contribution foreign key (parent_id) references contribution (contribution_id);
-- 91.sql
ALTER TABLE contribution DROP CONSTRAINT ck_contribution_contribution_status;
ALTER TABLE contribution ADD CONSTRAINT ck_contribution_contribution_status CHECK (status::text =
ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text,
'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text,
'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text,
'FORKED_PRIVATE_DRAFT'::character varying::text, 'FORKED_PUBLIC_DRAFT'::character varying::text,
'FORKED_PUBLISHED'::character varying::text, 'MERGED_PRIVATE_DRAFT'::character varying::text,
'MERGED_PUBLIC_DRAFT'::character varying::text]));

-- 92.sql
ALTER TABLE contribution ALTER COLUMN status TYPE character varying(20);

-- 93.sql
ALTER TABLE contribution_status_audit DROP CONSTRAINT ck_contribution_status_audit_status;
ALTER TABLE contribution_status_audit ADD CONSTRAINT ck_contribution_status_audit_status (status::text =
ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text,
'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text,
'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text,
'FORKED_PRIVATE_DRAFT'::character varying::text, 'FORKED_PUBLIC_DRAFT'::character varying::text,
'FORKED_PUBLISHED'::character varying::text, 'MERGED_PRIVATE_DRAFT'::character varying::text,
'MERGED_PUBLIC_DRAFT'::character varying::text]));

-- 94.sql
CREATE OR REPLACE FUNCTION add_proposals_from_theme_to_wg
  (theme_id bigint, wg_id bigint, create_memberships boolean) RETURNS void AS $$
     DECLARE

       contribution contribution%ROWTYPE;
       author appcivist_user%ROWTYPE;
     BEGIN

       CREATE TEMPORARY TABLE temp_table  ON COMMIT DROP
         as select * from contribution
         where resource_space_resource_space_id
               in (
                 select distinct resource_space_resource_space_id
                 from resource_space_theme
                 where theme_theme_id = theme_id
          ) and type = 4 and
            NOT EXISTS ( select rs.working_group_group_id from
       resource_space_working_groups rs where rs.resource_space_resource_space_id = resource_space_resource_space_id
            and rs.working_group_group_id = wg_id);

       insert into resource_space_working_groups
         select resource_space_resource_space_id, wg_id from temp_table;

       IF create_memberships THEN
             FOR contribution IN SELECT * from temp_table
              LOOP
                for author in select * from contribution_appcivist_user where
                  contribution_contribution_id  = contribution.contribution_id and
                    NOT EXISTS (select * from membership where user_user_id = author.user_id
                    and working_group_group_id = wg_id)
                  LOOP
                      insert into membership(membership_type, status, creator_user_id, user_user_id, working_group_group_id)
                      VALUES ('GROUP', 'ACCEPTED', author.user_id, author.user_id, wg_id);
                end loop;
              END LOOP;
       end if;
     END;
 $$ LANGUAGE plpgsql;

-- 95.sql
ALTER TABLE working_group_profile add COLUMN auto_accept_membership boolean default true;

-- 96.sql
CREATE OR REPLACE FUNCTION add_proposals_from_theme_to_wg
  (theme_id bigint, wg_id bigint, create_memberships boolean) RETURNS void AS $$
DECLARE

contribution contribution%ROWTYPE;
author appcivist_user%ROWTYPE;

numberContributions BIGINT;
membershipId BIGINT;
has_role BIGINT;
groupRsId BIGINT;

BEGIN
raise notice 'Creating temporary table for contributions in Theme = % ...', theme_id;

CREATE TEMPORARY TABLE temp_table  ON COMMIT DROP
  as select * from contribution
  where resource_space_resource_space_id
        in (
          select distinct resource_space_resource_space_id
          from resource_space_theme
          where theme_theme_id = theme_id
        ) and type = 4 and
        NOT EXISTS ( select wg.group_id from working_group wg, resource_space_contributions rs
        where wg.resources_resource_space_id = rs.resource_space_resource_space_id
              and rs.contribution_contribution_id = contribution_id
              and wg.group_id = wg_id);

SELECT count(*) FROM temp_table into numberContributions;
SELECT resources_resource_space_id from working_group where group_id = wg_id into groupRsId;

RAISE NOTICE 'Inserting % contributions INTO working GROUP % using RS % ...', numberContributions, wg_id, groupRsId;

insert into resource_space_contributions
  select groupRsId, contribution_id from temp_table;

IF create_memberships THEN
FOR contribution IN SELECT * from temp_table
LOOP
for author in select appcivist_user_user_id as user_id, contribution_contribution_id from contribution_appcivist_user where
  contribution_contribution_id  = contribution.contribution_id and
  NOT EXISTS (select * from membership where user_user_id = author.user_id
                                             and working_group_group_id = wg_id)
LOOP

select membership_id from membership
where membership_type = 'GROUP'
      and creator_user_id = author.user_id
      and user_user_id = author.user_id
      and working_group_group_id = wg_id
into membershipId;

IF membershipId > 0 THEN
RAISE NOTICE 'User % already a member of group %',author.user_id, wg_id;
ELSE
RAISE NOTICE 'Inserting membership for user % in GROUP % ...',author.user_id, wg_id;
insert into membership(membership_type, status, creator_user_id, user_user_id, working_group_group_id)
VALUES ('GROUP', 'ACCEPTED', author.user_id, author.user_id, wg_id);

select membership_id from membership
where membership_type = 'GROUP'
      and creator_user_id = author.user_id
      and user_user_id = author.user_id
      and working_group_group_id = wg_id
into membershipId;

RAISE NOTICE 'New Membership ID  for user % is % ...',author.user_id, membershipId;


select membership_membership_id from membership_role where membership_membership_id = membershipId
into has_role;

IF has_role > 0 THEN
RAISE NOTICE 'Membership ID for user % already has role % ...',author.user_id, membershipId;
ELSE
RAISE NOTICE 'Inserting role for user % in membership % ...',author.user_id, membershipId;
insert into membership_role values(membershipId, 4);
end if;
end if;
end loop;
END LOOP;
end if;
END;
$$ LANGUAGE plpgsql;


-- 98.sql
ALTER TABLE contribution DROP CONSTRAINT ck_contribution_contribution_status;

UPDATE contribution set status = 'MERGED' where STATUS ilike 'MERGE%';

ALTER TABLE contribution ADD CONSTRAINT ck_contribution_contribution_status CHECK (status::text =
ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text,
'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text,
'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text,
'FORKED_PRIVATE_DRAFT'::character varying::text, 'FORKED_PUBLIC_DRAFT'::character varying::text,
'FORKED_PUBLISHED'::character varying::text, 'MERGED'::character varying::text]));


ALTER TABLE contribution_status_audit DROP CONSTRAINT ck_contribution_status_audit_status;

UPDATE contribution_status_audit set status = 'MERGED' where STATUS ilike 'MERGE%';

ALTER TABLE contribution_status_audit ADD CONSTRAINT ck_contribution_status_audit_status CHECK (status::text =
ANY (ARRAY['NEW'::character varying::text, 'DRAFT'::character varying::text, 'PUBLISHED'::character varying::text,
'ARCHIVED'::character varying::text, 'EXCLUDED'::character varying::text, 'PUBLIC_DRAFT'::character varying::text,
'MODERATED'::character varying::text, 'INBALLOT'::character varying::text, 'SELECTED'::character varying::text,
'FORKED_PRIVATE_DRAFT'::character varying::text, 'FORKED_PUBLIC_DRAFT'::character varying::text,
'FORKED_PUBLISHED'::character varying::text, 'MERGED'::character varying::text]));

-- 99.sql
insert into  subscription
    (space_type, subscription_type, user_id, space_id)
    (
        select distinct 'CONTRIBUTION',
                        'REGULAR',
                        au.uuid             as user_id,
                        resource_space.uuid as resource_space_id
        from contribution
                 join resource_space on contribution.resource_space_resource_space_id = resource_space.resource_space_id
                 join resource_space_working_groups rswg
                      on contribution.resource_space_resource_space_id = rswg.resource_space_resource_space_id
                 join membership on rswg.working_group_group_id = membership.working_group_group_id
                 join appcivist_user au on membership.user_user_id = au.user_id
        where (contribution.status = 'PUBLISHED'
            or contribution.status = 'PUBLIC_DRAFT')
          and contribution.removed is false
    );

-- 100.sql 
CREATE OR REPLACE FUNCTION generate_proposal_source_codes
  (campaignID bigint, replace_existing_codes boolean) RETURNS void AS $$
DECLARE

BEGIN
  raise notice 'Generating source codes for campaig = % ...', campaignID;

  IF replace_existing_codes THEN
    UPDATE contribution co
    SET source_code =  
      CASE 
          WHEN c.status in ('PUBLISHED', 'DRAFT', 'EXCLUDED', 'ARCHIVED', 'NEW', 'PUBLIC_DRAFT', 'INBALLOT') and c.parent_id is null then 'P'||c.contribution_id 
          WHEN c.status not in ('FORKED_PUBLISHED') and c.parent_id is not null then 'P'||c.parent_id
          ELSE 'P'||c.parent_id||'.'||to_char((select count(*) from contribution c2 where c2.contribution_id<c.contribution_id and c2.parent_id = c.parent_id and c2.status = 'FORKED_PUBLISHED')::integer + 1,'09')
      END
    FROM contribution c
    JOIN resource_space_contributions rsc on c.contribution_id = rsc.contribution_contribution_id 
    JOIN campaign ca on rsc.resource_space_resource_space_id = ca.resources_resource_space_id 
    WHERE ca.campaign_id = campaignID and co.contribution_id = c.contribution_id;
  ELSE
    UPDATE contribution co
    SET source_code =  
      CASE 
          WHEN c.status in ('PUBLISHED', 'DRAFT', 'EXCLUDED', 'ARCHIVED', 'NEW', 'PUBLIC_DRAFT', 'INBALLOT') and c.parent_id is null then 'P'||c.contribution_id 
          WHEN c.status not in ('FORKED_PUBLISHED') and c.parent_id is not null then 'P'||c.parent_id
          ELSE 'P'||c.parent_id||'.'||to_char((select count(*) from contribution c2 where c2.contribution_id<c.contribution_id and c2.parent_id = c.parent_id and c2.status = 'FORKED_PUBLISHED')::integer + 1,'09')
      END
    FROM contribution c
    JOIN resource_space_contributions rsc on c.contribution_id = rsc.contribution_contribution_id 
    JOIN campaign ca on rsc.resource_space_resource_space_id = ca.resources_resource_space_id 
    WHERE ca.campaign_id = campaignID and co.contribution_id = c.contribution_id and c.source_code is null;
  END IF;
END;
$$ LANGUAGE plpgsql;


-- 101.sql
create or replace function naturalorder(text)
    returns bytea language sql immutable strict as $f$
    select string_agg(convert_to(coalesce(r[2], length(length(r[1])::text) || length(r[1])::text || r[1]), 'SQL_ASCII'),'\x00')
    from regexp_matches($1, '0*([0-9]+)|([^0-9]+)', 'g') r;
$f$;

-- 102.sql
INSERT INTO security_role ("role_id", "name") VALUES (DEFAULT, 'JURY');

-- 103.sql

create table contribution_jury (
    id                              bigserial not null,
    creation                        timestamp,
    last_update                     timestamp,
    lang                            varchar(255),
    removal                         timestamp,
    removed                         boolean,
    user_id                         bigint not null,
    contribution_id                 bigint not null,
    constraint pk_contribution_jury primary key (id)

);

alter table contribution_jury add constraint fk_contribution_jury_user foreign key (user_id) references appcivist_user (user_id);
alter table contribution_jury add constraint fk_contribution_jury_contribution foreign key (contribution_id) references contribution (contribution_id);

-- 104.sql
ALTER TABLE contribution_jury DROP CONSTRAINT fk_contribution_jury_user;
ALTER TABLE contribution_jury DROP COLUMN user_id;
ALTER TABLE contribution_jury ADD COLUMN user_id bigint;
alter table contribution_jury add constraint fk_contribution_jury_user foreign key (user_id) references appcivist_user (user_id);
ALTER TABLE contribution_jury ADD COLUMN username varchar(100);