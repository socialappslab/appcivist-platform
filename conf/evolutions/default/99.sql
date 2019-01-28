# --- !Ups
create table initial_data_config (
  data_file_id              bigserial not null,
  data_file                 varchar(255),
  loaded                    boolean,
  constraint pk_initial_data_config primary key (data_file_id))
;

# --- !Downs
drop table initial_data_config;