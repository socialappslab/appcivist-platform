# --- !Ups
create table resource_space_association_history (
  id                        bigserial not null,
  creation                  timestamp,
  resource_space_resource_space_id      bigint,
  entity_id                 bigint,
  entity_type               varchar(40),
  constraint pk_asocciation primary key (id));

alter table resource_space_association_history add constraint fk_resource_space_association_history_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table appcivist_user add column facebook_user_id varchar(20);
alter table appcivist_user add column user_access_token varchar(256);
alter table appcivist_user add column token_expires_in varchar(20);


# --- !Downs