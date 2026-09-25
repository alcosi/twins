package org.twins.core.mappers.rest.factory;

import org.springframework.stereotype.Component;
import org.twins.core.dao.factory.TwinFactoryEraserEntity;
import org.twins.core.dto.rest.factory.FactoryEraserUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FactoryEraserUpdateDTOReverseMapper extends RestSimpleDTOMapper<FactoryEraserUpdateDTOv1, TwinFactoryEraserEntity> {

    @Override
    public void map(FactoryEraserUpdateDTOv1 src, TwinFactoryEraserEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setInputTwinClassId(src.getInputTwinClassId())
                .setTwinFactoryConditionSetId(src.getTwinFactoryConditionSetId())
                .setTwinFactoryConditionInvert(src.getTwinFactoryConditionInvert())
                .setActive(src.getActive())
                .setDescription(src.getDescription())
                .setEraserAction(src.getAction());
    }
}
