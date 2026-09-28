package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryMultiplierUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierUpdateDTOv1, TwinFactoryMultiplierEntity> {

    @Override
    public void map(FactoryMultiplierUpdateDTOv1 src, TwinFactoryMultiplierEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInputTwinClassId(src.getInputTwinClassId())
                .setMultiplierFeaturerId(src.getMultiplierFeaturerId())
                .setMultiplierParams(src.getMultiplierParams())
                .setActive(src.getActive())
                .setDescription(src.getDescription());
    }
}
