package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryBranchCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchCreateRqDTOv1, TwinFactoryBranchEntity> {

    @Override
    public void map(FactoryBranchCreateRqDTOv1 src, TwinFactoryBranchEntity dst, MapperContext mapperContext) {
        dst
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.getActive())
                .setNextTwinFactoryId(src.getNextFactoryId())
                .setDescription(src.getDescription());
    }
}
