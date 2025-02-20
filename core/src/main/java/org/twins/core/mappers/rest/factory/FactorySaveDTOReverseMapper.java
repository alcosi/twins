package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEntity;
import org.twins.core.dto.rest.factory.FactoryCreateRqDTOv1;
import org.twins.core.dto.rest.factory.FactorySaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class FactorySaveDTOReverseMapper extends RestSimpleDTOMapper<FactorySaveRqDTOv1, TwinFactoryEntity> {

    @Override
    public void map(FactorySaveRqDTOv1 src, TwinFactoryEntity dst, MapperContext mapperContext) {
        dst
                .setKey(src.getKey());

    }
}
