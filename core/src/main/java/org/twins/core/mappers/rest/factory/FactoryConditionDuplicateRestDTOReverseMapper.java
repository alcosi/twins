package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryConditionDuplicate;
import org.twins.core.dto.rest.factory.FactoryConditionDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryConditionDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionDuplicateDTOv1, FactoryConditionDuplicate> {

    @Override
    public void map(FactoryConditionDuplicateDTOv1 src, FactoryConditionDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalFactoryConditionId())
                .setNewParentEntityId(src.getNewTwinFactoryConditionSetId());
    }
}
