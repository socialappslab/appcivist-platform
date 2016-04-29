# --- !Ups

alter table ballot alter column id set default nextval('ballots_id_seq');
alter table ballot_registration_field alter column id set default nextval('ballot_registration_fields_id_seq');
alter table ballot_configuration alter column id set default nextval('ballot_configurations_id_seq');
alter table ballot_paper alter column id set default nextval('ballot_papers_id_seq');
alter table candidate alter column id set default nextval('candidates_id_seq');
alter table vote alter column id set default nextval('votes_id_seq');
 
# --- !Downs