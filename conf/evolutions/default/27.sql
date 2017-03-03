# --- !Ups
--- Evolution related to #346, include comment count in contribution model
alter table contribution add column comment_count integer;
alter table contribution add column forum_comment_count integer;
update contribution set comment_count=0;
update contribution set forum_comment_count=0;

--- Evolution related to #365, adding the organization model
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

# --- !Downs
