# --- !Ups
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

# --- !Downs
