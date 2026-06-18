package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryMultiplierFilterDuplicate;
import org.twins.core.dto.rest.factory.FactoryMultiplierFilterDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierFilterDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierFilterDuplicateDTOv1, FactoryMultiplierFilterDuplicate> {

    @Override
    public void map(FactoryMultiplierFilterDuplicateDTOv1 src, FactoryMultiplierFilterDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalFactoryMultiplierFilterId())
                .setNewParentEntityId(src.getNewTwinFactoryMultiplierId());
    }
}
