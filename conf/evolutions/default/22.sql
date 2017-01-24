# --- !Ups
alter table appcivist_user add column creation timestamp;
alter table appcivist_user add column last_update timestamp;
alter table appcivist_user add column lang varchar(255);
alter table appcivist_user add column removal timestamp;
alter table appcivist_user add column removed boolean;
      
# --- Downs