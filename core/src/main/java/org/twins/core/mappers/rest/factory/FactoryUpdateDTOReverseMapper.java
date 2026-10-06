package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class FactoryUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryUpdateDTOv1, TwinFactoryEntity> {

    @Override
    public void map(FactoryUpdateDTOv1 src, TwinFactoryEntity dst, MapperContext mapperContext) {
        dst
                .setId(src.getId())
                .setKey(src.getKey())
                .setFactoryProcessorFeaturerId(src.getFactoryProcessorFeaturerId())
                .setFactoryProcessorParams(src.getFactoryProcessorParams());
    }
}
