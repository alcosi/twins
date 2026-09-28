package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryBranchUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchUpdateRqDTOv1, TwinFactoryBranchEntity> {

    @Override
    public void map(FactoryBranchUpdateRqDTOv1 src, TwinFactoryBranchEntity dst, MapperContext mapperContext) {
        dst
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.getActive())
                .setNextTwinFactoryId(src.getNextFactoryId())
                .setDescription(src.getDescription());
    }
}
