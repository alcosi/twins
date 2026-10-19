package org.twins.core.mappers.rest.businessaccount;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dto.rest.businessaccount.BusinessAccountUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;
import org.twins.core.service.businessaccount.BusinessAccountUserService;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = BusinessAccountUserMode.class)
public class BusinessAccountUserDTOMapper extends RestSimpleDTOMapper<BusinessAccountUserEntity, BusinessAccountUserDTOv1> {
    @MapperModePointerBinding(modes = UserMode.BusinessAccountUser2UserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    private final BusinessAccountUserService businessAccountUserService;

    @Override
    public void map(BusinessAccountUserEntity src, BusinessAccountUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(BusinessAccountUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setLastActivityAt(src.getLastActivityAt() != null ? src.getLastActivityAt().toLocalDateTime() : null);
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId())
                        .setBusinessAccountId(src.getBusinessAccountId());
                break;
        }
        if (mapperContext.hasModeButNot(UserMode.BusinessAccountUser2UserMode.HIDE)) {
            businessAccountUserService.loadUser(src);
            dst.setUserId(src.getUserId());
            userRestDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.BusinessAccountUser2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.HIDE)) {
            businessAccountUserService.loadBusinessAccount(src);
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.SHORT)));
        }

    }

    @Override
    public void beforeCollectionConversion(Collection<BusinessAccountUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.BusinessAccountUser2UserMode.HIDE))
            businessAccountUserService.loadUser(srcCollection);
        if (mapperContext.hasModeButNot(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.HIDE))
            businessAccountUserService.loadBusinessAccount(srcCollection);
    }
}
