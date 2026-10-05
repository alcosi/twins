package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupUpdate;
import org.twins.core.dto.rest.usergroup.UserGroupUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupUpdateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupUpdateDTOv1, UserGroupUpdate> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(UserGroupUpdateDTOv1 src, UserGroupUpdate dst, MapperContext mapperContext) throws Exception {
        dst.setId(src.getId());
        dst.setBusinessAccountId(src.getBusinessAccountId())
           .setNameI18n(i18NSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
           .setDescriptionI18n(i18NSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext));
    }
}
