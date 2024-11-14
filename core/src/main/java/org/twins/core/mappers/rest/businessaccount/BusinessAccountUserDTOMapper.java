package org.twins.core.mappers.rest.businessaccount;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.businessaccount.BusinessAccountUserEntity;
import org.twins.core.dto.rest.domain.BusinessAccountUserDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountUserMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = BusinessAccountUserMode.class)
public class BusinessAccountUserDTOMapper extends RestSimpleDTOMapper<BusinessAccountUserEntity, BusinessAccountUserDTOv1> {

    @Override
    public void map(BusinessAccountUserEntity src, BusinessAccountUserDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(BusinessAccountUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setUserId(src.getUserId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst.setId(src.getId());
                break;
        }
    }
}
