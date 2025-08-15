package org.twins.core.mappers.rest.twinflow;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TransitionTriggerSearch;
import org.twins.core.dto.rest.transition.TransitionTriggerSearchDTOv1;
import org.twins.core.dto.rest.transition.TransitionTriggerSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@AllArgsConstructor
public class TransitionTriggerSearchDTOReverseMapper extends RestSimpleDTOMapper<TransitionTriggerSearchDTOv1, TransitionTriggerSearch> {

    @Override
    public void map(TransitionTriggerSearchDTOv1 src, TransitionTriggerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinflowTransitionIdList(src.getTwinflowTransitionIdList())
                .setTwinflowTransitionIdExcludeList(src.getTwinflowTransitionIdExcludeList())
                .setTransitionTriggerFeaturerIdList(src.getTransitionTriggerFeaturerIdList())
                .setTransitionTriggerFeaturerIdExcludeList(src.getTransitionTriggerFeaturerIdExcludeList())
                .setActive(src.getActive());
    }
}
