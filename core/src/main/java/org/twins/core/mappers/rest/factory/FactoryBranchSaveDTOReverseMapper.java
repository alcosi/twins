package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryBranchEntity;
import org.twins.core.dto.rest.factory.FactoryBranchSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryBranchSaveDTOReverseMapper extends RestSimpleDTOMapper<FactoryBranchSaveRqDTOv1, TwinFactoryBranchEntity> {

    @Override
    public void map(FactoryBranchSaveRqDTOv1 src, TwinFactoryBranchEntity dst, MapperContext mapperContext) {
        dst
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getFactoryConditionSetInvert())
                .setActive(src.isActive())
                .setNextTwinFactoryId(src.getNextFactoryId())
                .setDescription(src.getDescription());
    }
}
