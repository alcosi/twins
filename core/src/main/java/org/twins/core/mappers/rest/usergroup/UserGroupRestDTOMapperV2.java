package org.twins.core.mappers.rest.usergroup;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.user.UserGroupEntity;
import org.twins.core.dto.rest.usergroup.UserGroupDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.service.usergroup.UserGroupService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class UserGroupRestDTOMapperV2 extends RestSimpleDTOMapper<UserGroupEntity, UserGroupDTOv2> {

    private final UserGroupRestDTOMapper userGroupRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.UserGroup2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final UserGroupService userGroupService;

    @Override
    public void map(UserGroupEntity src, UserGroupDTOv2 dst, MapperContext mapperContext) throws Exception {
        userGroupRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(BusinessAccountMode.UserGroup2BusinessAccountMode.HIDE)) {
            userGroupService.loadBusinessAccount(src);
            dst
                    .setBusinessAccount(businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.UserGroup2BusinessAccountMode.SHORT))))
                    .setBusinessAccountId(src.getBusinessAccountId());
        }
    }

    @Override
    public void beforeCollectionConversion(Collection<UserGroupEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        userGroupRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(BusinessAccountMode.UserGroup2BusinessAccountMode.HIDE))
            userGroupService.loadBusinessAccount(srcCollection);
    }

    @Override
    public String getObjectCacheId(UserGroupEntity src) {
        return src.getId().toString();
    }

}
