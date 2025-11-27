package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryConditionEntity;
import org.twins.core.dto.rest.factory.FactoryConditionUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryConditionUpdateRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionUpdateDTOv1, TwinFactoryConditionEntity> {

    private final FactoryConditionSaveRestDTOReverseMapper factoryConditionSaveRestDTOReverseMapper;

    @Override
    public void map(FactoryConditionUpdateDTOv1 src, TwinFactoryConditionEntity dst, MapperContext mapperContext) throws Exception {
        factoryConditionSaveRestDTOReverseMapper.map(src, dst, mapperContext);
        dst.setId(src.getId());
    }
}
