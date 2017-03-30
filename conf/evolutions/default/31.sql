# --- !Ups
create table resource_space_custom_field_value (
  resource_space_resource_space_id                                                 bigint not null,
  custom_field_value_custom_field_value_id                                         bigint not null,
  constraint pk_resource_space_custom_field_value primary key (resource_space_resource_space_id, custom_field_value_custom_field_value_id))
;

alter table resource_space_custom_field_value add constraint fk_resource_space_custom_value_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_custom_field_value add constraint fk_resource_space_custom_value_02 foreign key (custom_field_value_custom_field_value_id) references custom_field_value (custom_field_value_id);

ALTER TABLE "public"."custom_field_value" ADD CONSTRAINT "fk_custom_field_definition_01" FOREIGN KEY ("custom_field_definition_id") REFERENCES "public"."custom_field_definition"("custom_field_definition_id");
ALTER TABLE "public"."custom_field_definition" RENAME COLUMN "position" TO "field_position";
ALTER TABLE "public"."custom_field_definition" RENAME COLUMN "limit" TO "field_limit";
ALTER TABLE "public"."custom_field_value" ALTER COLUMN "value" SET DATA TYPE text;

alter table working_group add column is_topic boolean;
update working_group set is_topic=false;


# --- !Downs
