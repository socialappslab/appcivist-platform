# --- !Ups

insert into contribution_appcivist_user (contribution_contribution_id, appcivist_user_user_id)
select c.contribution_id, b.user_id from non_member_author a join appcivist_user b on a.email = b.email
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user;

delete  from contribution_non_member_author a where (non_member_author_id, contribution_id)
in
                                                   (select c.non_member_author_id, c.contribution_id from non_member_author a join appcivist_user b on a.email = b.email
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user);


insert into contribution_appcivist_user (contribution_contribution_id, appcivist_user_user_id)
select c.contribution_id, b.user_id from non_member_author a join appcivist_user b on a.name = b.name and a.email is null
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user;

delete  from contribution_non_member_author a where (non_member_author_id, contribution_id)
in
                                                   (select c.non_member_author_id, c.contribution_id from non_member_author a join appcivist_user b on a.name = b.name and a.email is null
join contribution_non_member_author c on c.non_member_author_id = a.id  except
select contribution_contribution_id, appcivist_user_user_id from contribution_appcivist_user);

# --- !Downs
