package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.holder.I18nCacheHolder;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;
import org.twins.core.service.i18n.I18nService;

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
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()));
                break;
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(I18nCacheHolder.addId(src.getNameI18NId()))
                        .setDescription(I18nCacheHolder.addId(src.getDescriptionI18NId()))
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setType(src.getUserGroupTypeId());
                break;
        }
    }

    @Override
    public String getObjectCacheId(UserGroupEntity src) {
        return src.getId().toString();
    }


}
