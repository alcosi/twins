package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TwinflowMode.class)
public class TwinflowBaseV1RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv1> {

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinflowMode.SHORT)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setTwinClassId(src.getTwinClassId())
                        .setName(src.getName())
                        .setCreatedAt(src.getCreatedAt().toLocalDateTime())
                        .setCreatedByUserId(src.getCreatedByUserId())
                        .setInitialStatusId(src.getInitialTwinStatusId());
                break;
            case SHORT:
                dst
                        .setName(src.getName())
                        .setId(src.getId());
                break;
        }
    }

    @Override
    public String getObjectCacheId(TwinflowEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(TwinflowMode.HIDE);
    }

}
