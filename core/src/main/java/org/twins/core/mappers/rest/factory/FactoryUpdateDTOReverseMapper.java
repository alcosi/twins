package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactoryUpdateRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class FactoryUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryUpdateRqDTOv1, TwinFactoryEntity> {

    private final FactorySaveDTOReverseMapper factorySaveDTOReverseMapper;

    @Override
    public void map(FactoryUpdateRqDTOv1 src, TwinFactoryEntity dst, MapperContext mapperContext) {
        factorySaveDTOReverseMapper.map(src, dst, mapperContext);
    }
}
