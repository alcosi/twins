package org.twins.core.mappers.rest.twinflow;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinflowFactorySearch;
import org.twins.core.dto.rest.twinflow.TwinflowFactorySearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinflowFactorySearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinflowFactorySearchRqDTOv1, TwinflowFactorySearch> {

    @Override
    public void map(TwinflowFactorySearchRqDTOv1 src, TwinflowFactorySearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinflowIdList(src.getTwinflowIdList())
                .setTwinflowIdExcludeList(src.getTwinflowIdExcludeList())
                .setTwinFactoryIdList(src.getTwinFactoryIdList())
                .setTwinFactoryIdExcludeList(src.getTwinFactoryIdExcludeList())
                .setFactoryLauncherList(src.getFactoryLauncherList())
                .setFactoryLauncherExcludeList(src.getFactoryLauncherExcludeList());
    }
}
