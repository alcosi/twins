package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.DomainBusinessAccountEntity;
import org.twins.core.dto.rest.domain.DomainBusinessAccountDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainBusinessAccountMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = {DomainBusinessAccountMode.class})
public class DomainBusinessAccountDTOMapper extends RestSimpleDTOMapper<DomainBusinessAccountEntity, DomainBusinessAccountDTOv1> {

    @MapperModePointerBinding(modes = {BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.class})
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(DomainBusinessAccountEntity src, DomainBusinessAccountDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainBusinessAccountMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setTwinflowSchemaId(src.getTwinflowSchemaId())
                        .setTwinClassSchemaId(src.getTwinClassSchemaId())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime());
                break;
            case SHORT:
                dst
                        .setId(src.getId());
                break;
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.HIDE)) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(BusinessAccountMode.DomainBusinessAccount2BusinessAccountMode.SHORT));
        }


    }
}
