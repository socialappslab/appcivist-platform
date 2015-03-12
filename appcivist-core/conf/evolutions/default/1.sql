# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table assembly (
  id                        bigint not null,
  name                      varchar(255),
  description               varchar(255),
  city                      varchar(255),
  icon                      varchar(255),
  url                       varchar(255),
  constraint pk_assembly primary key (id))
;

create table campaign (
  id                        bigint not null,
  assembly_id               bigint not null,
  name                      varchar(255),
  url                       varchar(255),
  constraint pk_campaign primary key (id))
;

create table Linked_Account (
  account_id                bigint not null,
  user_id                   bigint,
  provider_user_id          varchar(255),
  provider_key              varchar(255),
  constraint pk_Linked_Account primary key (account_id))
;

create table Token_Action (
  token_id                  bigint not null,
  token                     varchar(255),
  user_id                   bigint,
  type                      varchar(2),
  created                   timestamp,
  expires                   timestamp,
  constraint ck_Token_Action_type check (type in ('PR','EV')),
  constraint uq_Token_Action_token unique (token),
  constraint pk_Token_Action primary key (token_id))
;

create table appcivist_user (
  user_id                   bigint not null,
  email                     varchar(255),
  name                      varchar(255),
  username                  varchar(255),
  locale                    varchar(255),
  email_verified            boolean,
  username_verified         boolean,
  profile_pic               varchar(255),
  conf_type                 varchar(255),
  constraint pk_appcivist_user primary key (user_id))
;

create sequence assembly_seq;

create sequence campaign_seq;

create sequence Linked_Account_seq;

create sequence Token_Action_seq;

create sequence appcivist_user_seq;

alter table campaign add constraint fk_campaign_assembly_1 foreign key (assembly_id) references assembly (id);
create index ix_campaign_assembly_1 on campaign (assembly_id);
alter table Linked_Account add constraint fk_Linked_Account_user_2 foreign key (user_id) references appcivist_user (user_id);
create index ix_Linked_Account_user_2 on Linked_Account (user_id);
alter table Token_Action add constraint fk_Token_Action_targetUser_3 foreign key (user_id) references appcivist_user (user_id);
create index ix_Token_Action_targetUser_3 on Token_Action (user_id);



# --- !Downs

drop table if exists assembly cascade;

drop table if exists campaign cascade;

drop table if exists Linked_Account cascade;

drop table if exists Token_Action cascade;

drop table if exists appcivist_user cascade;

drop sequence if exists assembly_seq;

drop sequence if exists campaign_seq;

drop sequence if exists Linked_Account_seq;

drop sequence if exists Token_Action_seq;

drop sequence if exists appcivist_user_seq;

