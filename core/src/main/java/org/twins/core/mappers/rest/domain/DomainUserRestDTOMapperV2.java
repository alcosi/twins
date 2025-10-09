package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainUserEntity;
import org.twins.core.dto.rest.domain.DomainUserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountUserDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserCollectionMode;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

import java.util.Collection;


@Component
@RequiredArgsConstructor
public class DomainUserRestDTOMapperV2 extends RestSimpleDTOMapper<DomainUserEntity, DomainUserDTOv2> {

    private final DomainUserRestDTOMapper domainUserRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.DomainUser2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountUserMode.DomainUser2BusinessAccountUserMode.class)
    private final BusinessAccountUserDTOMapper businessAccountUserDTOMapper;

    @Override
    public void map(DomainUserEntity src, DomainUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        domainUserRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(BusinessAccountUserMode.DomainUser2BusinessAccountUserMode.HIDE) && mapperContext.hasModeButNot(BusinessAccountUserCollectionMode.HIDE))
            dst
                    .setBusinessAccountUsers(businessAccountUserDTOMapper.convertCollectionPostpone(src.getBusinessAccountUserKit().getCollection(), mapperContext.forkOnPoint(BusinessAccountUserMode.DomainUser2BusinessAccountUserMode.SHORT)))
                    .setBusinessAccountUserIdList(src.getBusinessAccountUserKit().getIdSet());
    }

    @Override
    public void beforeCollectionConversion(Collection<DomainUserEntity> srcCollection, MapperContext mapperContext) throws Exception {
        super.beforeCollectionConversion(srcCollection, mapperContext);
        domainUserRestDTOMapper.beforeCollectionConversion(srcCollection, mapperContext);
    }
}
