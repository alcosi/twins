package org.twins.core.mappers.rest.space;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.i18n.I18nEntity;
import org.twins.core.dao.space.SpaceRoleEntity;
import org.twins.core.domain.space.SpaceRoleCreate;
import org.twins.core.dto.rest.space.SpaceRoleCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class SpaceRoleCreateDTOReverseMapper extends RestSimpleDTOMapper<SpaceRoleCreateDTOv1, SpaceRoleCreate> {
    private final I18nSaveRestDTOReverseMapper i18nSaveRestDTOReverseMapper;

    @Override
    public void map(SpaceRoleCreateDTOv1 src, SpaceRoleCreate dst, MapperContext mapperContext) throws Exception {
        I18nEntity nameI18n = i18nSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext);
        I18nEntity descriptionI18n = i18nSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext);
        dst
                .setNameI18n(nameI18n)
                .setDescriptionI18n(descriptionI18n)
                .setSpaceRole(
                new SpaceRoleEntity()
                        .setKey(src.getKey())
                        .setTwinClassId(src.getTwinClassId())
                        .setBusinessAccountId(src.getBusinessAccountId()));
    }
}
