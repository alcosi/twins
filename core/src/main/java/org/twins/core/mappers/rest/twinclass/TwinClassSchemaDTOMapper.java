package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassSchemaEntity;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassSchemaMode;

@Component
@RequiredArgsConstructor
public class TwinClassSchemaDTOMapper extends RestSimpleDTOMapper<TwinClassSchemaEntity, TwinClassSchemaDTOv1> {
    @Override
    public void map(TwinClassSchemaEntity src, TwinClassSchemaDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TwinClassSchemaMode.DETAILED)) {
            case DETAILED -> dst
                        .setId(src.getId())
                        .setDomainId(src.getDomainId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setCreatedByUserId(src.getCreatedByUserId());
            case SHORT -> dst
                        .setId(src.getId())
                        .setDomainId(src.getDomainId());
        }
    }
}
