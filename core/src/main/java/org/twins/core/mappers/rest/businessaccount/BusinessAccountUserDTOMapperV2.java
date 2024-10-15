package org.twins.core.mappers.rest.businessaccount;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dto.rest.domain.BusinessAccountUserDTOv1;
import org.twins.core.dto.rest.domain.BusinessAccountUserDTOv2;
import org.twins.core.dto.rest.domain.DomainUserDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.class})
public class BusinessAccountUserDTOMapperV2 extends RestSimpleDTOMapper<BusinessAccountUserEntity, BusinessAccountUserDTOv2> {

    @MapperModePointerBinding(modes = UserMode.BusinessAccountUser2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.BusinessAccount2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(BusinessAccountUserEntity src, BusinessAccountUserDTOv2 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(BusinessAccountMode.BusinessAccountUser2BusinessAccountMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                if (src.getUser() != null)
                    dst.setUser((userDTOMapper.convertOrPostpone(src.getUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.BusinessAccountUser2UserMode.SHORT)))));



                if (src.getBusinessAccount() != null)
                    dst.setBusinessAccount(businessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.BusinessAccount2BusinessAccountMode.SHORT))));






                break;
            case SHORT:
                dst.setId(src.getId());
                break;
        }
    }
}
