package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryEraserDuplicate;
import org.twins.core.dto.rest.factory.FactoryEraserDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryEraserDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryEraserDuplicateDTOv1, FactoryEraserDuplicate> {

    @Override
    public void map(FactoryEraserDuplicateDTOv1 src, FactoryEraserDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalFactoryEraserId(src.getOriginalFactoryEraserId())
                .setNewTwinFactoryId(src.getNewTwinFactoryId());
    }
}
