# --- !Ups
create table contribution_feedback (
    id                              bigserial not null,
    creation                        timestamp,
    last_update                     timestamp,
    lang                            varchar(255),
    removal                         timestamp,
    removed                         boolean,
    user_id                         bigint not null,
    contribution_id                 bigint not null,
    up                              boolean,
    down                            boolean,
    fav                             boolean,
    flag                            boolean,    
    constraint pk_contribution_feedback primary key (id)
    
);

alter table contribution_feedback add constraint fk_contribution_feedback_user foreign key (user_id) references appcivist_user (user_id);
alter table contribution_feedback add constraint fk_contribution_feedback_contribution foreign key (contribution_id) references contribution (contribution_id);
alter table contribution drop constraint "ck_contribution_type";
alter table contribution add constraint "ck_contribution_type" check (type = ANY (ARRAY[0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11]));

# --- !Downs
drop table if exists contribution_feedback cascade;
