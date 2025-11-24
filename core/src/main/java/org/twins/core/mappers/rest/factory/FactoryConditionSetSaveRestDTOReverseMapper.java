package org.twins.core.mappers.rest.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryConditionSetEntity;
import org.twins.core.dto.rest.factory.FactoryConditionSetSaveDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class FactoryConditionSetSaveRestDTOReverseMapper extends RestSimpleDTOMapper<FactoryConditionSetSaveDTOv1, TwinFactoryConditionSetEntity> {

    @Override
    public void map(FactoryConditionSetSaveDTOv1 src, TwinFactoryConditionSetEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setName(src.getName())
                .setDescription(src.getDescription());
    }
}
