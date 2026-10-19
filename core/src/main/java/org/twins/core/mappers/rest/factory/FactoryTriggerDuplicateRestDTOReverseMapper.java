package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryTriggerDuplicate;
import org.twins.core.dto.rest.factory.FactoryTriggerDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryTriggerDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryTriggerDuplicateDTOv1, FactoryTriggerDuplicate> {

    @Override
    public void map(FactoryTriggerDuplicateDTOv1 src, FactoryTriggerDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalFactoryTriggerId())
                .setNewParentEntityId(src.getNewTwinFactoryId());
    }
}
