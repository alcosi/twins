package org.twins.core.mappers.rest.twinflow;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldNameConstants;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinflow.TwinflowEntity;
import org.twins.core.dto.rest.twinflow.TwinflowBaseDTOv3;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.MapperModePointer;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twinflow.TwinflowTransitionService;


@Component
@RequiredArgsConstructor
public class TwinflowBaseV3RestDTOMapper extends RestSimpleDTOMapper<TwinflowEntity, TwinflowBaseDTOv3> {
    final TwinflowBaseV2RestDTOMapper twinflowBaseV2RestDTOMapper;
    final TransitionBaseV2RestDTOMapper transitionBaseV2RestDTOMapper;
    final TwinflowTransitionService twinflowTransitionService;

    @Override
    public void map(TwinflowEntity src, TwinflowBaseDTOv3 dst, MapperContext mapperContext) throws Exception {
        twinflowBaseV2RestDTOMapper.map(src, dst, mapperContext);
        if (showTransitions(mapperContext)) {
                twinflowTransitionService.loadAllTransitions(src);
                dst
                        .setTransitions(transitionBaseV2RestDTOMapper.convertMap(src.getTransitionsKit().getMap(), mapperContext.forkOnPoint(TwinflowTransitionMode.HIDE)));
        }
    }

    private static boolean showTransitions(MapperContext mapperContext) {
        return !mapperContext.hasModeOrEmpty(TwinflowTransitionMode.HIDE);
    }

    @Override
    public String getObjectCacheId(TwinflowEntity src) {
        return twinflowBaseV2RestDTOMapper.getObjectCacheId(src);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return twinflowBaseV2RestDTOMapper.hideMode(mapperContext);
    }

    @AllArgsConstructor
    @FieldNameConstants(onlyExplicitlyIncluded = true)
    public enum TwinflowTransitionMode implements MapperModePointer<TransitionBaseV1RestDTOMapper.Mode> {
        @FieldNameConstants.Include HIDE(0),
        @FieldNameConstants.Include SHORT(1),
        @FieldNameConstants.Include DETAILED(2);

        @Getter
        final int priority;

        @Override
        public TransitionBaseV1RestDTOMapper.Mode point() {
            return switch (this) {
                case HIDE -> TransitionBaseV1RestDTOMapper.Mode.HIDE;
                case SHORT -> TransitionBaseV1RestDTOMapper.Mode.SHORT;
                case DETAILED -> TransitionBaseV1RestDTOMapper.Mode.DETAILED;
            };
        }
    }
}
