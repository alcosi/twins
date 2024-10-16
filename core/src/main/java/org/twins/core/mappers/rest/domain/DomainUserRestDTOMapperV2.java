package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountUserDTOMapperV2;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DomainUserMode.class})
public class DomainUserRestDTOMapperV2 extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv2> {

    @MapperModePointerBinding(modes = UserMode.DomainUser2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    private final DomainUserRestDTOMapper domainUserRestDTOMapper;

    private final BusinessAccountUserDTOMapperV2 businessAccountUserDTOMapperV2;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        domainUserRestDTOMapper.map(src, dst, mapperContext);
        switch (mapperContext.getModeOrUse(DomainUserMode.DETAILED)) {
            case DETAILED:
                dst.setUser(userDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DomainUser2UserMode.SHORT))));
                break;
            case SHORT:
        }
        if (showBusinessAccountUser(mapperContext)) {
            dst.setBusinessAccountUserIdList(src.getBusinessAccountUserKit().getIdSet());
            dst.setBusinessAccountUsers(businessAccountUserDTOMapperV2.convertCollection(src.getBusinessAccountUserKit().getCollection(), mapperContext));
        }
    }

    private static boolean showBusinessAccountUser(MapperContext mapperContext) {
        return mapperContext.hasModeButNot(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.HIDE);
    }
}
