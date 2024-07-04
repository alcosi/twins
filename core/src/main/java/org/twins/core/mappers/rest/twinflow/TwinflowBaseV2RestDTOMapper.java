package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv2;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.twinstatus.TwinStatusRestDTOMapper;
import org.twins.core.mappers.rest.user.UserRestDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinflowBaseV2RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv2> {

    @MapperModePointerBinding(modes = MapperMode.TwinflowInitStatusMode.class)
    private final TwinStatusRestDTOMapper twinStatusRestDTOMapper;

    private final TwinflowBaseV1RestDTOMapper twinflowBaseV1RestDTOMapper;

    @MapperModePointerBinding(modes = MapperMode.TwinflowUserMode.class)
    private final UserRestDTOMapper userRestDTOMapper;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv2 dst, MapperContext mapperContext) throws Exception {
        twinflowBaseV1RestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(MapperMode.TwinflowUserMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setCreatedByUser(userRestDTOMapper.convertOrPostpone(src.getCreatedByUser(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TwinflowUserMode.SHORT))))
                    .setCreatedByUserId(src.getCreatedByUserId());
        if (mapperContext.hasModeButNot(MapperMode.TwinflowInitStatusMode.HIDE) && src.getCreatedByUserId() != null)
            dst
                    .setInitialStatus(twinStatusRestDTOMapper.convertOrPostpone(src.getInitialTwinStatus(), mapperContext.forkOnPoint(mapperContext.getModeOrUse(MapperMode.TwinflowInitStatusMode.SHORT))))
                    .setInitialStatusId(src.getInitialTwinStatusId());
    }

    @Override
    public String getObjectCacheId(TwinflowEntity src) {
        return twinflowBaseV1RestDTOMapper.getObjectCacheId(src);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinflowBaseV1RestDTOMapper.hideMode(mapperContext);
    }

}
