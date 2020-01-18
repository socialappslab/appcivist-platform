# --- !Ups
create table contribution_jury (
    id                              bigserial not null,
    creation                        timestamp,
    last_update                     timestamp,
    lang                            varchar(255),
    removal                         timestamp,
    removed                         boolean,
    user_id                         bigint not null,
    contribution_id                 bigint not null,
    constraint pk_contribution_jury primary key (id)

);

alter table contribution_jury add constraint fk_contribution_jury_user foreign key (user_id) references appcivist_user (user_id);
alter table contribution_jury add constraint fk_contribution_jury_contribution foreign key (contribution_id) references contribution (contribution_id);

# --- !Downs
drop table if exists contribution_jury cascade;
