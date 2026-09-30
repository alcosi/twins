package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryBranchUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchUpdateDTOv1, TwinFactoryBranchEntity> {

    @Override
    public void map(FactoryBranchUpdateDTOv1 src, TwinFactoryBranchEntity dst, MapperContext mapperContext) {
        dst
                .setId(src.getId())
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.getActive())
                .setNextTwinFactoryId(src.getNextFactoryId())
                .setDescription(src.getDescription());
    }
}
