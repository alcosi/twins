package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserCreateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryEraserCreateDTOReverseMapper extends RestSimpleDTOMapper<FactoryEraserCreateDTOv1, TwinFactoryEraserEntity> {

    @Override
    public void map(FactoryEraserCreateDTOv1 src, TwinFactoryEraserEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setActive(src.getActive())
                .setDescription(src.getDescription())
                .setEraserAction(src.getAction());
    }
}
