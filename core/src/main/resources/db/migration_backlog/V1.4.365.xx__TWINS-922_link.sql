-- update link_validator
alter table link_validator rename column link_validator_featurer_id to linker_featurer_id;
alter table link_validator rename column link_validator_params to linker_params;

-- update link
alter table link drop column linker_params;
drop index idx_link_linker_featurer_id;
alter table link drop constraint link_featurer_id_fk;
alter table link drop column linker_featurer_id;
