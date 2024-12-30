package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.cambium.i18n.service.I18nService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dao.user.UserGroupTypeEntity;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = UserGroupMode.class)
public class UserGroupRestDTOMapper extends RestSimpleDTOMapper<UserGroupEntity, UserGroupDTOv1> {
    private final I18nService i18nService;

    @Override
    public void map(UserGroupEntity src, UserGroupDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(UserGroupMode.SHORT)) {
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18NId()));
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(i18nService.translateToLocale(src.getNameI18NId()))
                        .setDescription(i18nService.translateToLocale(src.getDescriptionI18NId()))
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setType(UserGroupTypeEntity.UserGroupType.valueOf(src.getUserGroupTypeId()));
                break;
        }
    }

    @Override
    public String getObjectCacheId(UserGroupEntity src) {
        return src.getId().toString();
    }


}
