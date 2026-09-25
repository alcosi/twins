package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryMultiplierCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierCreateDTOv1, TwinFactoryMultiplierEntity> {

    @Override
    public void map(FactoryMultiplierCreateDTOv1 src, TwinFactoryMultiplierEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInputTwinClassId(src.getInputTwinClassId())
                .setMultiplierFeaturerId(src.getMultiplierFeaturerId())
                .setMultiplierParams(src.getMultiplierParams())
                .setActive(src.getActive())
                .setDescription(src.getDescription());
    }
}
