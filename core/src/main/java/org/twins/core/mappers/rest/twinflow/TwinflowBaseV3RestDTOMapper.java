package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv3;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twinflow.TwinflowTransitionService;


@Component
@RequiredArgsConstructor
public class TwinflowBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv3> {
    final TwinflowBaseV2RestDTOMapper twinflowBaseV2RestDTOMapper;
    final TwinflowTransitionBaseV2RestDTOMapper twinflowTransitionBaseV2RestDTOMapper;
    final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinflowBaseV2RestDTOMapper.map(src, dst, mapperContext);
        if (!twinflowTransitionBaseV2RestDTOMapper.hideMode(mapperContext)) {
                twinflowTransitionService.loadAllTransitions(src);
                dst
                        .setTransitions(twinflowTransitionBaseV2RestDTOMapper.convertMap(src.getTransitionsKit().getMap(), mapperContext));
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
