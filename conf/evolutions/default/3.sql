# TODO: 
# --- !Ups

alter table ballot add column require_registration boolean;
alter table ballot add column user_uuid_as_signature boolean;
alter table ballot add column decision_type varchar(40);
alter table campaign add column consultive_ballot varchar(40);
alter table ballot add column component bigint;
alter table campaign add column binding_ballot varchar(40);
alter table working_group add column consensus_ballot varchar(40);
 
# --- !Downs
