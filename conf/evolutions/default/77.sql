# --- !Ups

alter table campaign_participation drop CONSTRAINT pk_campaign_participation;
alter TABLE campaign_participation add COLUMN campaign_participation_id bigserial;
UPDATE campaign_participation SET campaign_participation_id=nextval('campaign_participation_campaign_participation_id_seq');
alter TABLE campaign_participation add CONSTRAINT pk_campaign_participation primary key (campaign_participation_id);

# --- !Downs
