package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupCreate;
import org.twins.core.dto.rest.usergroup.UserGroupCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupCreateDTOReverseMapper extends RestSimpleDTOMapper<UserGroupCreateDTOv1, UserGroupCreate> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(UserGroupCreateDTOv1 src, UserGroupCreate dst, MapperContext mapperContext) throws Exception {
        dst.setUserGroupTypeId(src.getUserGroupTypeId());
        dst.setBusinessAccountId(src.getBusinessAccountId())
           .setNameI18n(i18NSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
           .setDescriptionI18n(i18NSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext));
    }
}
