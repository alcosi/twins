package org.twins.core.mappers.rest.trigger;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinTriggerSearch;
import org.twins.core.dto.rest.trigger.TwinTriggerSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinTriggerSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinTriggerSearchDTOv1, TwinTriggerSearch> {
    @Override
    public void map(TwinTriggerSearchDTOv1 src, TwinTriggerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTriggerFeaturerIdList(src.getTriggerFeaturerIdList())
                .setTriggerFeaturerIdExcludeList(src.getTriggerFeaturerIdExcludeList())
                .setActive(src.getActive())
                .setNameLikeList(src.getNameLikeList());
    }
}
