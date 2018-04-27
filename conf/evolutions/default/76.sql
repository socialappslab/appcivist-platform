create table campaign_participation (
    user_user_id             bigint,
    campaign_campaign_id     bigint,
    creation_date            timestamp default now(),
    user_consent             boolean,
    user_provided_consent    boolean,
    constraint pk_campaign_participation primary key (user_user_id, campaign_campaign_id),
    constraint fk_campaign_participation_user foreign key (user_user_id) references appcivist_user (user_id),
    constraint fk_campaign_participation_campaign foreign key (campaign_campaign_id) references campaign (campaign_id)
);