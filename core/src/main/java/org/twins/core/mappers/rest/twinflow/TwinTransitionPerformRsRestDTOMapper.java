package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinflow.TwinTransitionPerformRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionResultMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.service.twinflow.TwinflowTransitionService;

import java.util.*;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionResultMode.class)
public class TwinTransitionPerformRsRestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionService.TransitionResult, TwinTransitionPerformRsDTOv1> {

    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;

    @Override
    public void map(TwinflowTransitionService.TransitionResult src, TwinTransitionPerformRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TransitionResultMode.DETAILED)) {
            case DETAILED:
                List<TwinDTOv2> processedList = twinRestDTOMapperV2.convertCollection(src.getProcessedTwinList(), mapperContext);

                if (CollectionUtils.isNotEmpty(processedList)) {
                    Map<UUID, List<TwinDTOv2>> processedGroupedByClass = new HashMap<>();
                    for (TwinDTOv2 twinDTOv2 : processedList) {
                        List<TwinDTOv2> twinsGroupedByClass = processedGroupedByClass.computeIfAbsent(twinDTOv2.twinClassId(), k -> new ArrayList<>());
                        twinsGroupedByClass.add(twinDTOv2);
                    }
                    dst.setProcessedTwinList(processedGroupedByClass);
                }
            case SHORT:
                dst.setTransitionedTwinList(twinRestDTOMapperV2.convertCollection(src.getTransitionedTwinList(), mapperContext));
        }
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(StatusMode.HIDE);
    }

}
