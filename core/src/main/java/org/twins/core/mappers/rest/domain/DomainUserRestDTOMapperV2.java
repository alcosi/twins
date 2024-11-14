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
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserCollectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.Collection;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = DomainUserMode.class)
public class DomainUserRestDTOMapperV2 extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv2> {

    private final DomainUserRestDTOMapper domainUserRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.DomainUser2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    private final BusinessAccountUserDTOMapperV2 businessAccountUserDTOMapperV2;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        domainUserRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.DomainUser2UserMode.HIDE))
            dst
                    .setUser(userDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.DomainUser2UserMode.SHORT))))
                    .setUserId(src.getUserId());
        if (mapperContext.hasModeButNot(BusinessAccountUserCollectionMode.HIDE))
            dst
                    .setBusinessAccountUsers(businessAccountUserDTOMapperV2.convertCollection(src.getBusinessAccountUserKit().getCollection(), mapperContext))
                    .setBusinessAccountUserIdList(src.getBusinessAccountUserKit().getIdSet());
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        domainUserRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
    }
}
