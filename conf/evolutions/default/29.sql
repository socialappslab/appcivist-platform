# --- !Ups
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
  "position"                  integer,
  "limit"                     text,
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

# --- !Downs
