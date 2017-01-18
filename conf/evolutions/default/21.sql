# --- !Ups
alter table assembly add column principalassembly boolean default FALSE;
      
# --- Downs