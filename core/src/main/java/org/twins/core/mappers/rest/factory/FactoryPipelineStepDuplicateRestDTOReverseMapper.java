package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryPipelineStepDuplicate;
import org.twins.core.dto.rest.factory.FactoryPipelineStepDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryPipelineStepDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryPipelineStepDuplicateDTOv1, FactoryPipelineStepDuplicate> {

    @Override
    public void map(FactoryPipelineStepDuplicateDTOv1 src, FactoryPipelineStepDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalFactoryPipelineStepId())
                .setDuplicateParentEntityId(src.getNewTwinFactoryPipelineId());
    }
}
