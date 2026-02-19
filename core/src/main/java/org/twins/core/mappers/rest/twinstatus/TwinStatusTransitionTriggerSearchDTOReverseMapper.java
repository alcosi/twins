package org.twins.core.mappers.rest.twinstatus;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinStatusTransitionTriggerSearch;
import org.twins.core.dto.rest.twinstatus.TwinStatusTransitionTriggerSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinStatusTransitionTriggerSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTransitionTriggerSearchDTOv1, TwinStatusTransitionTriggerSearch> {

    @Override
    public void map(TwinStatusTransitionTriggerSearchDTOv1 src, TwinStatusTransitionTriggerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinStatusIdList(src.getTwinStatusIdList())
                .setTwinStatusIdExcludeList(src.getTwinStatusIdExcludeList())
                .setTypeList(src.getTypeList())
                .setTypeExcludeList(src.getTypeExcludeList())
                .setTwinTriggerIdList(src.getTwinTriggerIdList())
                .setTwinTriggerIdExcludeList(src.getTwinTriggerIdExcludeList())
                .setActive(src.getActive())
                .setAsync(src.getAsync());
    }
}
