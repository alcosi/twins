package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.domain.space.SpaceRoleSave;
import org.twins.core.dto.rest.space.SpaceRoleSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class SpaceRoleSaveDTOReverseMapper extends RestSimpleDTOMapper<SpaceRoleSaveDTOv1, SpaceRoleSave> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(SpaceRoleSaveDTOv1 src, SpaceRoleSave dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18n = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18n = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setNameI18n(nameI18n)
                .setDescriptionI18n(descriptionI18n)
                .setSpaceRole(
                new SpaceRoleEntity()
                        .setNameI18n(nameI18n)
                        .setDescriptionI18n(descriptionI18n)
                        .setKey(src.getKey())
                        .setTwinClassId(src.getTwinClassId())
                        .setBusinessAccountId(src.getBusinessAccountId()));
    }
}
