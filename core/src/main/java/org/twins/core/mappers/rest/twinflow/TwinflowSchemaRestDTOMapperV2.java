package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.businessaccount.BusinessAccountDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.BusinessAccountMode;
import org.twins.core.mappers.rest.mappercontext.modes.UserMode;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;

@Component
@RequiredArgsConstructor
public class TwinflowSchemaRestDTOMapperV2 extends RestSimpleDTOMapper<TwinflowSchemaEntity, TwinflowSchemaDTOv2> {

    private final TwinflowSchemaRestDTOMapper twinflowSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = UserMode.TwinflowSchema2UserMode.class)
    private final UserRestDTOMapper userDTOMapper;

    @MapperModePointerBinding(modes = BusinessAccountMode.TwinflowSchema2BusinessAccountMode.class)
    private final BusinessAccountDTOMapper BusinessAccountDTOMapper;

    @Override
    public void map(TwinflowSchemaEntity src, TwinflowSchemaDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinflowSchemaRestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(UserMode.TwinflowSchema2UserMode.HIDE))
            dst
                    .setCreatedByUser(userDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(UserMode.TwinflowSchema2UserMode.SHORT))))
                    .setCreatedByUserId(src.getCreatedByUserId());
        if (mapperContext.hasModeButNot(BusinessAccountMode.TwinflowSchema2BusinessAccountMode.HIDE))
            dst
                    .setBusinessAccount(BusinessAccountDTOMapper.convertOrPostpone(src.getBusinessAccount(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(BusinessAccountMode.TwinflowSchema2BusinessAccountMode.SHORT))))
                    .setBusinessAccountId(src.getBusinessAccountId());
    }
}
