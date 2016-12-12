# --- !Ups
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

# --- !Downs
drop table contribution_publish_history;

alter table contribution add column popularity integer;