# --- !Ups
alter table campaign add column logo_resource_id bigint;
alter table campaign add constraint fk_campaign_resource_logo foreign key (logo_resource_id) references resource (resource_id);

# --- !Downs
