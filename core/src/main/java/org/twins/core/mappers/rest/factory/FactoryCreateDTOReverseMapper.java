package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryCreateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class FactoryCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryCreateRqDTOv1, TwinFactoryEntity> {

    @Override
    public void map(FactoryCreateRqDTOv1 src, TwinFactoryEntity dst, MapperContext mapperContext) {
        dst
                .setKey(src.getKey())
                .setFactoryProcessorFeaturerId(src.getFactoryProcessorFeaturerId())
                .setFactoryProcessorParams(src.getFactoryProcessorParams());
    }
}
