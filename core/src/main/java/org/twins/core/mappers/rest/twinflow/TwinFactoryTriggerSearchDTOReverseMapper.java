package org.twins.core.mappers.rest.twinflow;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinFactoryTriggerSearch;
import org.twins.core.dto.rest.twinflow.TwinFactoryTriggerSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinFactoryTriggerSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinFactoryTriggerSearchDTOv1, TwinFactoryTriggerSearch> {

    @Override
    public void map(TwinFactoryTriggerSearchDTOv1 src, TwinFactoryTriggerSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinFactoryIdList(src.getTwinFactoryIdList())
                .setTwinFactoryIdExcludeList(src.getTwinFactoryIdExcludeList())
                .setInputTwinClassIdList(src.getInputTwinClassIdList())
                .setInputTwinClassIdExcludeList(src.getInputTwinClassIdExcludeList())
                .setTwinTriggerIdList(src.getTwinTriggerIdList())
                .setTwinTriggerIdExcludeList(src.getTwinTriggerIdExcludeList())
                .setActive(src.getActive())
                .setAsync(src.getAsync());
    }
}
