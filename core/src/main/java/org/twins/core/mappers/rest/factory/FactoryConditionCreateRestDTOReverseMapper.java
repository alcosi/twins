package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dto.rest.factory.FactoryConditionCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryConditionCreateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionCreateDTOv1, TwinFactoryConditionEntity> {

    @Override
    public void map(FactoryConditionCreateDTOv1 src, TwinFactoryConditionEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinFactoryConditionSetId(src.getFactoryConditionSetId())
                .setConditionerFeaturerId(src.getConditionerFeatureId())
                .setConditionerParams(src.getConditionerParams())
                .setDescription(src.getDescription())
                .setActive(src.getActive())
                .setInvert(src.getInvert());
    }
}
