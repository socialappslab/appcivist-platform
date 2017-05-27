# --- !Ups
alter table campaign add column cover_resource_id bigint;
alter table campaign add constraint fk_contribution_resource_cover foreign key (cover_resource_id) references resource (resource_id);

# --- !Downs
