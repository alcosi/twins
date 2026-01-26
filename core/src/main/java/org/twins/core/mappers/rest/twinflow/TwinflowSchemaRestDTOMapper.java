package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.DomainUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinflowSchemaMode.class)
public class TwinflowSchemaRestDTOMapper extends RestSimpleDTOMapper<TwinflowSchemaEntity, TwinflowSchemaDTOv1> {

    @MapperModePointerBinding(modes = UserMode.TwinflowSchema2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.TwinflowSchema2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper businessAccountDTOMapper;

    @Override
    public void map(TwinflowSchemaEntity src, TwinflowSchemaDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(DomainUserMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setDomainId(src.getDomainId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setBusinessAccountId(src.getBusinessAccountId())
                        .setCreatedByUserId(src.getCreatedByUserId());
                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                        .setBusinessAccountId(src.getBusinessAccountId());
                break;
        }

        // Postpone related objects according to modes
        if (mapperContext.hasModeButNot(UserMode.TwinflowSchema2UserMode.HIDE) && src.getCreatedByUserId() != null) {
            dst.setCreatedByUserId(src.getCreatedByUserId());
            userDTOMapper.postpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinflowSchema2UserMode.SHORT)));
        }
        if (mapperContext.hasModeButNot(BusinessAccountMode.TwinflowSchema2BusinessAccountMode.HIDE) && src.getBusinessAccountId() != null) {
            dst.setBusinessAccountId(src.getBusinessAccountId());
            businessAccountDTOMapper.postpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.TwinflowSchema2BusinessAccountMode.SHORT)));
        }
    }
}
