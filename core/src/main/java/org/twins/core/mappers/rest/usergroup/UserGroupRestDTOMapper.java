package org.twins.core.mappers.rest.usergroup;

import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.UserGroupMode;

@Component
@MapperModeBinding(modes = UserGroupMode.class)
public class UserGroupRestDTOMapper extends RestSimpleDTOMapper<UserGroupEntity, UserGroupDTOv1> {
    @Override
    public void map(UserGroupEntity src, UserGroupDTOv1 dst, MapperContext mapperContext) {
        switch (mapperContext.getModeOrUse(UserGroupMode.SHORT)) {
            case SHORT:
                dst
                        .id(src.getId())
                        .name(src.getName());
                break;
            case DETAILED:
                dst
                        .id(src.getId())
                        .name(src.getName())
                        .businessAccountId(src.getBusinessAccountId())
                        .type(src.getUserGroupTypeId());
                break;
        }
    }

    @Override
    public String getObjectCacheId(UserGroupEntity src) {
        return src.getId().toString();
    }


}
