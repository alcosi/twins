package org.twins.core.mappers.rest.twinstatus;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinStatusTriggerSearch;
import org.twins.core.dto.rest.twinstatus.TwinStatusTriggerSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinStatusTriggerSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinStatusTriggerSearchDTOv1, TwinStatusTriggerSearch> {

    @Override
    public void map(TwinStatusTriggerSearchDTOv1 src, TwinStatusTriggerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinStatusIdList(src.getTwinStatusIdList())
                .setTwinStatusIdExcludeList(src.getTwinStatusIdExcludeList())
                .setIncomingElseOutgoing(src.getIncomingElseOutgoing())
                .setTwinTriggerIdList(src.getTwinTriggerIdList())
                .setTwinTriggerIdExcludeList(src.getTwinTriggerIdExcludeList())
                .setActive(src.getActive())
                .setAsync(src.getAsync());
    }
}
