package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryBranchCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchCreateDTOv1, TwinFactoryBranchEntity> {

    @Override
    public void map(FactoryBranchCreateDTOv1 src, TwinFactoryBranchEntity dst, MapperContext mapperContext) {
        dst
                .setTwinFactoryId(src.getFactoryId())
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.getActive())
                .setNextTwinFactoryId(src.getNextFactoryId())
                .setDescription(src.getDescription());
    }
}
