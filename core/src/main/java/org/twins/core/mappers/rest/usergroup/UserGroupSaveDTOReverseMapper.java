package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.usergroup.UserGroupSave;
import org.twins.core.dto.rest.usergroup.UserGroupSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.i18n.I18nSaveRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class UserGroupSaveDTOReverseMapper extends RestSimpleDTOMapper<UserGroupSaveDTOv1, UserGroupSave> {
    private final I18nSaveRestDTOReverseMapper i18NSaveRestDTOReverseMapper;

    @Override
    public void map(UserGroupSaveDTOv1 src, UserGroupSave dst, MapperContext mapperContext) throws Exception {
        dst.setBusinessAccountId(src.getBusinessAccountId())
           .setNameI18n(i18NSaveRestDTOReverseMapper.convert(src.getNameI18n(), mapperContext))
           .setDescriptionI18n(i18NSaveRestDTOReverseMapper.convert(src.getDescriptionI18n(), mapperContext));
    }
}
