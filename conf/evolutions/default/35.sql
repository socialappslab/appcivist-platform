# --- !Ups
ALTER TABLE token_action DROP CONSTRAINT ck_Token_Action_type;
ALTER TABLE token_action ADD CONSTRAINT ck_Token_Action_type check (type in ('PR','MR','MI','EV', 'FT'));
ALTER TABLE contribution add column cover_resource_id bigint;

ALTER TABLE contribution add constraint fk_contribution_resource_cover foreign key (cover_resource_id) references resource (resource_id);

# --- !Downs