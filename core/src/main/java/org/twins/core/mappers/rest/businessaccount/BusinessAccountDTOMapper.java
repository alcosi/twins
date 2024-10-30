package org.twins.core.mappers.rest.businessaccount;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.businessaccount.BusinessAccountEntity;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {BusinessAccountMode.class})
public class BusinessAccountDTOMapper extends RestSimpleDTOMapper<BusinessAccountEntity, BusinessAccountDTOv1> {

    @Override
    public void map(BusinessAccountEntity src, BusinessAccountDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(BusinessAccountMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst.setId(src.getId());
                break;
        }
    }
}
