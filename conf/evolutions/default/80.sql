# --- !Ups
alter table contribution_non_member_author drop constraint fk_contribution_non_member_author_02;
alter table contribution_non_member_author   add CONSTRAINT fk_contribution_non_member_author_02 FOREIGN KEY (non_member_author_id)
      REFERENCES public.non_member_author (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE CASCADE;

# --- !Downs
