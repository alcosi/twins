package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryPipelineDuplicate;
import org.twins.core.dto.rest.factory.FactoryPipelineDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineDuplicateDTOv1, FactoryPipelineDuplicate> {

    @Override
    public void map(FactoryPipelineDuplicateDTOv1 src, FactoryPipelineDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalFactoryPipelineId(src.getOriginalFactoryPipelineId())
                .setNewTwinFactoryId(src.getNewTwinFactoryId());
    }
}
