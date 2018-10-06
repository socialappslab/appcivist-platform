ALTER TABLE contribution ADD column parent_id bigint;
ALTER TABLE contribution ADD constraint fk_contribution_contribution foreign key (parent_id) references contribution (contribution_id);