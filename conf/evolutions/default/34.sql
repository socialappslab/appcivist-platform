# --- !Ups
create table resource_space_association_history (
  id                        bigserial not null,
  creation                  timestamp,
  resource_space_resource_space_id      bigint,
  entity_id                 bigint,
  entity_type               varchar(40),
  constraint pk_asocciation primary key (id));

alter table resource_space_association_history add constraint fk_resource_space_association_history_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);


# --- !Downs
