# --- !Ups
-- This evolution is only for keeping evolutions not modified, the full create script already incorporates these changes on creation of the table
-- Change type of column 'type' in contribution feedback
ALTER TABLE contribution_feedback 
    ADD COLUMN type_tmp character varying (25);

UPDATE contribution_feedback SET type_tmp = cast(type as character varying (25));

ALTER table contribution_feedback
    DROP column type;
    
ALTER TABLE contribution_feedback 
    RENAME COLUMN type_tmp to type;
    
-- Change type of column 'status' in contribution feedback
ALTER TABLE contribution_feedback 
    ADD COLUMN status_tmp character varying (25);

UPDATE contribution_feedback SET status_tmp = cast(status as character varying (25));

ALTER table contribution_feedback
    DROP column status;
    
ALTER TABLE contribution_feedback 
    RENAME COLUMN status_tmp to status;
      
# --- !Downs
