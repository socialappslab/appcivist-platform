# --- !Ups
alter table ballot add column entity_type varchar(40);

ALTER TABLE "public"."candidate" RENAME COLUMN "contribution_uuid" TO "candidate_uuid";

alter table resource_space add column consensus_ballot character varying(40);


create table resource_space_ballot_history (
  resource_space_resource_space_id                                                 bigint not null,
  ballot_ballot_id                                                                 bigint not null,
  constraint pk_resource_space_ballot_history primary key (resource_space_resource_space_id, ballot_ballot_id))
;

alter table resource_space_ballot_history add constraint fk_resource_space_ballot_history_01 foreign key (resource_space_resource_space_id) references resource_space (resource_space_id);

alter table resource_space_ballot_history add constraint fk_resource_space_ballot_history_02 foreign key (ballot_ballot_id) references ballot (id);

# --- !Downs
