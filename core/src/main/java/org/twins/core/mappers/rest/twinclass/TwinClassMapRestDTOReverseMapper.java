package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassMap;
import org.twins.core.dto.rest.twinclass.TwinClassMapDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class TwinClassMapRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassMapDTOv1, TwinClassMap> {

    @Override
    public void map(TwinClassMapDTOv1 src, TwinClassMap dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setIncludeParentFields(src.getIncludeParentFields());
    }
}
