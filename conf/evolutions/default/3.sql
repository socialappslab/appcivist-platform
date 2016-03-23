# TODO: 
# --- !Ups

alter table ballot add column require_registration boolean;
alter table ballot add column user_uuid_as_signature boolean;
alter table ballot add column decision_type varchar(40);
alter table campaign add column ups_downs_ballot varchar(40);
 
# --- !Downs
drop index if exists ix_ballot_paper_signature;
drop index if exists ix_ballot_configuration_key;
