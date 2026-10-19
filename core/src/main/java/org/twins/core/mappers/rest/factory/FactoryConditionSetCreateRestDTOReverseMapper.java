package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryConditionSetCreateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionSetCreateDTOv1, TwinFactoryConditionSetEntity> {

    @Override
    public void map(FactoryConditionSetCreateDTOv1 src, TwinFactoryConditionSetEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setName(src.getName())
                .setDescription(src.getDescription())
                .setTwinFactoryId(src.getTwinFactoryId())
                .setCachable(src.getCachable());
    }
}
