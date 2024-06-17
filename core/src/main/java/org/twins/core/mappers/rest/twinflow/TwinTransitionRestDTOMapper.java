package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowTransitionEntity;
import org.twins.core.dto.rest.twinflow.TwinTransitionViewDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

@Component
@RequiredArgsConstructor
public class TwinTransitionRestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionEntity, TwinTransitionViewDTOv1> {
    final TransitionBaseV1RestDTOMapper transitionBaseV1RestDTOMapper;

    @Override
    public void map(TwinflowTransitionEntity src, TwinTransitionViewDTOv1 dst, MapperContext mapperContext) throws Exception {
        transitionBaseV1RestDTOMapper.map(src, dst, mapperContext);
    }

    @Override
    public String getObjectCacheId(TwinflowTransitionEntity src) {
        return src.getId().toString();
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return transitionBaseV1RestDTOMapper.hideMode(mapperContext);
    }
}
