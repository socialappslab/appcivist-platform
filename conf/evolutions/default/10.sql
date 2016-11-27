# --- !Ups
-- Adding persistence for notifications
create table notification_event_signal (
  id                        bigserial not null,
  creation                  timestamp,
  last_update               timestamp,
  lang                      varchar(255),
  removal                   timestamp,
  removed                   boolean,
  uuid                      varchar(40),
  origin                    varchar(40),
  origin_type               varchar(13),
  event_name                varchar(40),
  origin_name               varchar(255),
  title                     varchar(255),
  text                      text,
  resource_type             varchar(255), 
  resource_uuid             varchar(40),
  resource_title            varchar(255),
  resource_text             text,
  notification_date         timestamp,  
  associated_user           varchar(255),
  signaled                  boolean,
  constraint pk_notification_event primary key (id))
;

create index ix_notification_event_id on notification_event (id);
create index ix_notification_event_uuid on notification_event (uuid);
 
# --- !Downs
drop index ix_notification_event_id;
drop index ix_notification_event_uuid;
drop table notification_event;
