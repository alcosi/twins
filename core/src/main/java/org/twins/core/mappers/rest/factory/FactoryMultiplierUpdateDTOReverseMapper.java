package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryMultiplierEntity;
import org.twins.core.dto.rest.factory.FactoryMultiplierUpdateDTOv1;
import org.twins.core.dto.rest.factory.FactoryMultiplierUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryMultiplierUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryMultiplierUpdateDTOv1, TwinFactoryMultiplierEntity> {
    private final FactoryMultiplierSaveDTOReverseMapper factoryMultiplierSaveDTOReverseMapper;

    @Override
    public void map(FactoryMultiplierUpdateDTOv1 src, TwinFactoryMultiplierEntity dst, MapperContext mapperContext) throws Exception {
        factoryMultiplierSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
