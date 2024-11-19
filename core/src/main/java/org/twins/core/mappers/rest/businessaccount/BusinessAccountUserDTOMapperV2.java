package org.twins.core.mappers.rest.businessaccount;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dto.rest.domain.BusinessAccountUserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class BusinessAccountUserDTOMapperV2 extends RestSimpleDTOMapper<BusinessAccountUserEntity, BusinessAccountUserDTOv2> {

    private final BusinessAccountUserDTOMapper businessAccountUserDTOMapper;

    @MapperModePointerBinding(modes = UserMode.BusinessAccountUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(BusinessAccountUserEntity src, BusinessAccountUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        businessAccountUserDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.BusinessAccountUser2UserMode.HIDE))
            dst
                    .setUser(userRestDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.BusinessAccountUser2UserMode.SHORT))))
                    .setUserId(src.getUserId());
        if (mapperContext.hasModeButNot(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.HIDE))
            dst
                    .setBusinessAccount(businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.SHORT))))
                    .setBusinessAccountId(src.getBusinessAccountId());
    }
}
