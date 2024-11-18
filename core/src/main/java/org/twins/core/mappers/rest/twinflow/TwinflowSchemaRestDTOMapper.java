package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinflow.TwinflowSchemaEntity;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.DomainUserMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowSchemaMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinflowSchemaMode.class)
public class TwinflowSchemaRestDTOMapper extends RestSimpleDTOMapper<TwinflowSchemaEntity, TwinflowSchemaDTOv1> {

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
    }
}
