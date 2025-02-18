package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierSaveDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierSaveRqDTOv1, TwinFactoryMultiplierEntity> {

    @Override
    public void map(FactoryMultiplierSaveRqDTOv1 src, TwinFactoryMultiplierEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInputTwinClassId(src.getInputTwinClassId())
                .setMultiplierFeaturerId(src.getMultiplierFeaturerId())
                .setMultiplierParams(src.getMultiplierParams())
                .setActive(src.isActive())
                .setDescription(src.getDescription());
    }
}
