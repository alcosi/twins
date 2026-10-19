package org.twins.core.mappers.rest.trigger;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinTriggerTaskSearch;
import org.twins.core.dto.rest.trigger.TwinTriggerTaskSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinTriggerTaskSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerTaskSearchDTOv1, TwinTriggerTaskSearch> {
    @Override
    public void map(TwinTriggerTaskSearchDTOv1 src, TwinTriggerTaskSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinIdList(src.getTwinIdList())
                .setTwinIdExcludeList(src.getTwinIdExcludeList())
                .setTwinTriggerIdList(src.getTwinTriggerIdList())
                .setTwinTriggerIdExcludeList(src.getTwinTriggerIdExcludeList())
                .setPreviousTwinStatusIdList(src.getPreviousTwinStatusIdList())
                .setPreviousTwinStatusIdExcludeList(src.getPreviousTwinStatusIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList())
                .setBusinessAccountIdList(src.getBusinessAccountIdList())
                .setBusinessAccountIdExcludeList(src.getBusinessAccountIdExcludeList())
                .setStatusIdList(src.getStatusIdList())
                .setStatusIdExcludeList(src.getStatusIdExcludeList());
    }
}
