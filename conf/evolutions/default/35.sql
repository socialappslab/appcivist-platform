ALTER TABLE token_action DROP CONSTRAINT ck_Token_Action_type;
ALTER TABLE token_action ADD CONSTRAINT ck_Token_Action_type check (type in ('PR','MR','MI','EV', 'FT'));