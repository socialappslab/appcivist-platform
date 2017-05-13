# --- !Ups
alter table contribution add column cover_resource_id bigint;

alter table contribution add constraint fk_contribution_resource_cover foreign key (cover_resource_id) references resource (resource_id);

# --- !Downs
