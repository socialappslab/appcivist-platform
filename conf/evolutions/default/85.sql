# --- !Ups

ALTER TABLE s3file
RENAME TO appcivist_file;
alter TABLE appcivist_file add COLUMN target character varying(10) ;
alter TABLE appcivist_file add COLUMN url character varying(255) ;

# --- !Downs
