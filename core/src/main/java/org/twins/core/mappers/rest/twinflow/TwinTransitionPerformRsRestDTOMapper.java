package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinEntity;
import org.twins.core.domain.transition.TransitionContext;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twinflow.TwinTransitionContextDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionPerformRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.link.TwinLinkCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.attachment.AttachmentCUDRestDTOReverseMapper;
import org.twins.core.mappers.rest.twin.TwinFieldValueRestDTOReverseMapperV2;
import org.twins.core.mappers.rest.twin.TwinRestDTOMapperV2;
import org.twins.core.service.twin.TwinService;
import org.twins.core.service.twinflow.TwinflowTransitionService;
import org.twins.core.service.user.UserService;

import java.util.*;


@Component
@RequiredArgsConstructor
public class TwinTransitionPerformRsRestDTOMapper extends RestSimpleDTOMapper<TwinflowTransitionService.TransitionResult, TwinTransitionPerformRsDTOv1> {
    final TwinRestDTOMapperV2 twinRestDTOMapperV2;

    @Override
    public void map(TwinflowTransitionService.TransitionResult src, TwinTransitionPerformRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst.setTransitionedTwinList(twinRestDTOMapperV2.convertList(src.getTransitionedTwinList(), mapperContext));
        List<TwinDTOv2> processedList = twinRestDTOMapperV2.convertList(src.getProcessedTwinList(), mapperContext);

        if (CollectionUtils.isNotEmpty(processedList)) {
            Map<UUID, List<TwinDTOv2>> processedGroupedByClass = new HashMap<>();
            for (TwinDTOv2 twinDTOv2 : processedList) {
                List<TwinDTOv2> twinsGroupedByClass = processedGroupedByClass.computeIfAbsent(twinDTOv2.twinClassId(), k -> new ArrayList<>());
                twinsGroupedByClass.add(twinDTOv2);
            }
            dst.setProcessedTwinList(processedGroupedByClass);
        }
    }
}
