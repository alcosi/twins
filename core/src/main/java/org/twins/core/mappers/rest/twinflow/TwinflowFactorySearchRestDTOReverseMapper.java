package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinflowFactorySearch;
import org.twins.core.dto.rest.twinflow.TwinflowFactorySearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactorySearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactorySearchDTOv1, TwinflowFactorySearch> {

    @Override
    public void map(TwinflowFactorySearchDTOv1 src, TwinflowFactorySearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdSet(src.getIdSet())
                .setIdExcludeSet(src.getIdExcludeSet())
                .setTwinflowIdSet(src.getTwinflowIdSet())
                .setTwinflowIdExcludeSet(src.getTwinflowIdExcludeSet())
                .setTwinFactoryIdSet(src.getFactoryIdSet())
                .setTwinFactoryIdExcludeSet(src.getFactoryIdExcludeSet())
                .setFactoryLauncherSet(src.getFactoryLauncherSet())
                .setFactoryLauncherExcludeSet(src.getFactoryLauncherExcludeSet());
    }
}
