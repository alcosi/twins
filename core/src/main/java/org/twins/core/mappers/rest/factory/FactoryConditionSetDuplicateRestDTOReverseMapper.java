package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryConditionSetDuplicate;
import org.twins.core.dto.rest.factory.FactoryConditionSetDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryConditionSetDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionSetDuplicateDTOv1, FactoryConditionSetDuplicate> {

    @Override
    public void map(FactoryConditionSetDuplicateDTOv1 src, FactoryConditionSetDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setDuplicateConditions(src.isDuplicateConditions())
                .setOriginalEntityId(src.getOriginalFactoryConditionSetId())
                .setDuplicateParentEntityId(src.getNewTwinFactoryId());
    }
}
