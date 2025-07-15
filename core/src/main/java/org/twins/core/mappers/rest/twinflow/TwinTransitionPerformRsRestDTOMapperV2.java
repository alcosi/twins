package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.domain.transition.TransitionResult;
import org.twins.core.domain.transition.TransitionResultMajor;
import org.twins.core.domain.transition.TransitionResultMinor;
import org.twins.core.dto.rest.transition.TwinTransitionPerformResultMajorDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformResultMinorDTOv1;
import org.twins.core.dto.rest.transition.TwinTransitionPerformRsDTOv2;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.draft.DraftRestDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.StatusMode;
import org.twins.core.mappers.rest.mappercontext.modes.TransitionResultMode;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;

import java.util.*;


@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TransitionResultMode.class)
public class TwinTransitionPerformRsRestDTOMapperV2 extends RestSimpleDTOMapper<TransitionResult, TwinTransitionPerformRsDTOv2> {
    private final TwinRestDTOMapperV2 twinRestDTOMapperV2;
    private final DraftRestDTOMapper draftRestDTOMapper;

    @Override
    public void map(TransitionResult src, TwinTransitionPerformRsDTOv2 dst, MapperContext mapperContext) throws Exception {
        if (src instanceof TransitionResultMinor transitionResultMinor) {
            TwinTransitionPerformResultMinorDTOv1 transitionResultMinorDTO = new TwinTransitionPerformResultMinorDTOv1();
            switch (mapperContext.getModeOrUse(TransitionResultMode.DETAILED)) {
                case DETAILED:
                    List<TwinDTOv2> processedList = twinRestDTOMapperV2.convertCollection(transitionResultMinor.getProcessedTwinList(), mapperContext);
                    if (CollectionUtils.isNotEmpty(processedList)) {
                        Map<UUID, List<TwinDTOv2>> processedGroupedByClass = new HashMap<>();
                        for (TwinDTOv2 twinDTOv2 : processedList) {
                            List<TwinDTOv2> twinsGroupedByClass = processedGroupedByClass.computeIfAbsent(twinDTOv2.twinClassId(), k -> new ArrayList<>());
                            twinsGroupedByClass.add(twinDTOv2);
                        }
                        transitionResultMinorDTO.processedTwinList(processedGroupedByClass);
                    }
                    transitionResultMinorDTO.transitionedTwinList(twinRestDTOMapperV2.convertCollection(transitionResultMinor.getTransitionedTwinList(), mapperContext));
                    break;
                case SHORT:
                    transitionResultMinorDTO.transitionedTwinList(twinRestDTOMapperV2.convertCollection(transitionResultMinor.getTransitionedTwinList(), mapperContext));
                    break;
            }
            dst.setResult(transitionResultMinorDTO);
        } else if (src instanceof TransitionResultMajor transitionResultMajor) {
            TwinTransitionPerformResultMajorDTOv1 transitionResultMajorDTO = new TwinTransitionPerformResultMajorDTOv1()
                    .draft(draftRestDTOMapper.convert(transitionResultMajor.getCommitedDraftEntity()));
            dst.setResult(transitionResultMajorDTO);
        } else
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(StatusMode.HIDE);
    }

}
