package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dto.rest.factory.FactoryConditionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryConditionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionUpdateDTOv1, TwinFactoryConditionEntity> {

    @Override
    public void map(FactoryConditionUpdateDTOv1 src, TwinFactoryConditionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setConditionerFeaturerId(src.getConditionerFeatureId())
                .setConditionerParams(src.getConditionerParams())
                .setDescription(src.getDescription())
                .setActive(src.getActive())
                .setInvert(src.getInvert());
    }
}
