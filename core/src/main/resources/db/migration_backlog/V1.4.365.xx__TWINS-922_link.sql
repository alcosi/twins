-- TWINS-922: multiple linkers per link via the link_validator table.
-- link.linker_featurer_id/linker_params (single linker) are dropped; linker rules live in
-- link_validator rows (linker_featurer_id + linker_params + execution order), all must pass
-- on twin_link create/update (TwinLinkService.validateLinkByLinkers).

-- update link_validator
alter table link_validator rename column link_validator_featurer_id to linker_featurer_id;
alter table link_validator rename column link_validator_params to linker_params;
alter index idx_link_validator_link_validator_featurer_id rename to idx_link_validator_linker_featurer_id;

-- update link
alter table link drop column linker_params;
drop index idx_link_linker_featurer_id;
alter table link drop constraint link_featurer_id_fk;
alter table link drop column linker_featurer_id;
