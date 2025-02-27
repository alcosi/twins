package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryEraserCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryEraserCreateDTOv1, TwinFactoryEraserEntity> {
    private final FactoryEraserSaveDTOReverseMapper factoryEraserSaveDTOReverseMapper;

    @Override
    public void map(FactoryEraserCreateDTOv1 src, TwinFactoryEraserEntity dst, MapperContext mapperContext) throws Exception {
        factoryEraserSaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
