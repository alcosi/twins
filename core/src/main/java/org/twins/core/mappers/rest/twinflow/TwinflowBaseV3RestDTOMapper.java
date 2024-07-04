package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv3;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperMode;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twinflow.TwinflowTransitionService;


@Component
@RequiredArgsConstructor
public class TwinflowBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv3> {

    private final TwinflowBaseV2RestDTOMapper twinflowBaseV2RestDTOMapper;

    @MapperModePointerBinding(modes = MapperMode.TwinflowTransitionMode.class)
    private final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;

    private final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinflowBaseV2RestDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(MapperMode.TwinflowTransitionMode.HIDE)) {
                twinflowTransitionService.loadAllTransitions(src);
                dst
                        .setTransitions(transitionBaseV2RestDTOMapper.convertMap(src.getTransitionsKit().getMap(), mapperContext.forkOnPoint(MapperMode.TwinflowTransitionMode.HIDE)));
        }
    }

    @Override
    public String getObjectCacheId(TwinflowEntity src) {
        return twinflowBaseV2RestDTOMapper.getObjectCacheId(src);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinflowBaseV2RestDTOMapper.hideMode(mapperContext);
    }

}
