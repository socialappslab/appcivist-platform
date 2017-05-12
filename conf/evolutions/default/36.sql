# --- !Ups
create table contribution_non_member_author (
  contribution_id                                                 bigint not null,
  non_member_author_id                                            bigint not null,
  constraint pk_contribution_non_member_author primary key (contribution_id, non_member_author_id))
;

alter table contribution_non_member_author add constraint fk_contribution_non_member_author_01 foreign key (contribution_id) references contribution (contribution_id);

alter table contribution_non_member_author add constraint fk_contribution_non_member_author_02 foreign key (non_member_author_id) references non_member_author (id);

insert into contribution_non_member_author select contribution_id, non_member_author_id from contribution where non_member_author_id is not null;
# --- !Downs
