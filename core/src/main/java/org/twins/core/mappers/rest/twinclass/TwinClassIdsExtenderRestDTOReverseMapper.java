package org.twins.core.mappers.rest.twinclass;

import org.springframework.stereotype.Component;
import org.twins.core.domain.twinclass.TwinClassIdsExtender;
import org.twins.core.dto.rest.twinclass.TwinClassIdsExtenderDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
public class TwinClassIdsExtenderRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassIdsExtenderDTOv1, TwinClassIdsExtender> {

    @Override
    public void map(TwinClassIdsExtenderDTOv1 src, TwinClassIdsExtender dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassId(src.getTwinClassId())
                .setAddExtendableTwinClassIds(src.getAddExtendableTwinClassIds());
    }
}
