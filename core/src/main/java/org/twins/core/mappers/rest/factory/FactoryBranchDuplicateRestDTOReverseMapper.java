package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.factory.FactoryBranchDuplicate;
import org.twins.core.dto.rest.factory.FactoryBranchDuplicateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryBranchDuplicateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchDuplicateDTOv1, FactoryBranchDuplicate> {

    @Override
    public void map(FactoryBranchDuplicateDTOv1 src, FactoryBranchDuplicate dst, MapperContext mapperContext) throws Exception {
        dst
                .setOriginalEntityId(src.getOriginalFactoryBranchId())
                .setDuplicateParentEntityId(src.getNewTwinFactoryId());
    }
}
