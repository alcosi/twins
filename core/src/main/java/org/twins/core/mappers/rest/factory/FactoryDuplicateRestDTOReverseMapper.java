package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryDuplicate;
import org.twins.core.dto.rest.factory.FactoryDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryDuplicateDTOv1, FactoryDuplicate> {

    @Override
    public void map(FactoryDuplicateDTOv1 src, FactoryDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalFactoryId())
                .setNewKey(src.getNewKey());
        dst
                .setDuplicateBranches(src.isDuplicateBranches())
                .setDuplicateMultipliers(src.isDuplicateMultipliers())
                .setDuplicatePipelines(src.isDuplicatePipelines())
                .setDuplicateErasers(src.isDuplicateErasers())
                .setDuplicateTriggers(src.isDuplicateTriggers())
                .setDuplicateConditionSets(src.isDuplicateConditionSets());
    }
}
