package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
import org.cambium.common.kit.Kit;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.twin.TwinEntity;
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
import org.twins.core.mappers.rest.mappercontext.modes.TwinMode;
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
                    Kit<TwinDTOv2, UUID> processedList = new Kit<>(twinRestDTOMapperV2.convertCollection(transitionResultMinor.getProcessedTwinList(), mapperContext.forkOnPoint(TwinMode.TransitionResult2TwinMode.SHORT)), TwinDTOv2::id);
                    if (CollectionUtils.isNotEmpty(processedList)) {
                        Map<UUID, List<TwinDTOv2>> processedGroupedByClass = new HashMap<>();
                        for (TwinEntity twin : transitionResultMinor.getProcessedTwinList()) {
                            List<TwinDTOv2> twinsGroupedByClass = processedGroupedByClass.computeIfAbsent(twin.getTwinClassId(), k -> new ArrayList<>());
                            twinsGroupedByClass.add(processedList.get(twin.getId()));
                        }
                        transitionResultMinorDTO.setProcessedTwinList(processedGroupedByClass);
                    }
                    transitionResultMinorDTO.setTransitionedTwinList(twinRestDTOMapperV2.convertCollection(transitionResultMinor.getTransitionedTwinList(), mapperContext.forkOnPoint(TwinMode.TransitionResult2TwinMode.SHORT)));
                    break;
                case SHORT:
                    transitionResultMinorDTO.setTransitionedTwinList(twinRestDTOMapperV2.convertCollection(transitionResultMinor.getTransitionedTwinList(), mapperContext.forkOnPoint(TwinMode.TransitionResult2TwinMode.SHORT)));
                    break;
            }
            dst.setResult(transitionResultMinorDTO);
        } else if (src instanceof TransitionResultMajor transitionResultMajor) {
            TwinTransitionPerformResultMajorDTOv1 transitionResultMajorDTO = new TwinTransitionPerformResultMajorDTOv1()
                    .setDraft(draftRestDTOMapper.convert(transitionResultMajor.getCommitedDraftEntity()));
            dst.setResult(transitionResultMajorDTO);
        } else
            throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public boolean hideMode(MapperContext mapperContext) {
        return mapperContext.hasModeOrEmpty(StatusMode.HIDE);
    }

}
