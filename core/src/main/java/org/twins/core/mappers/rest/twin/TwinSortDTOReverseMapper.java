package org.twins.core.mappers.rest.twin;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinSort;
import org.twins.core.dto.rest.twin.TwinSortDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinSortDTOReverseMapper extends RestSimpleDTOMapper<TwinSortDTOv1, TwinSort> {
    @Override
    public void map(TwinSortDTOv1 src, TwinSort dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassFieldId(src.getTwinClassFieldId())
                .setDirection(src.getDirection());
    }
}
