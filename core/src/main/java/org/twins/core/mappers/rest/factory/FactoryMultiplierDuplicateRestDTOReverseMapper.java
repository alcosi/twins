package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryMultiplierDuplicate;
import org.twins.core.dto.rest.factory.FactoryMultiplierDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierDuplicateDTOv1, FactoryMultiplierDuplicate> {

    @Override
    public void map(FactoryMultiplierDuplicateDTOv1 src, FactoryMultiplierDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setDuplicateFilters(src.isDuplicateFilters())
                .setOriginalEntityId(src.getOriginalFactoryMultiplierId())
                .setDuplicateParentEntityId(src.getNewTwinFactoryId());
    }
}
