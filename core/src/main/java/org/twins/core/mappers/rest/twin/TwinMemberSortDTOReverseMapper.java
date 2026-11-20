package org.twins.core.mappers.rest.twin;

import org.springframework.stereotype.Component;
import org.twins.core.domain.twin.TwinMemberSort;
import org.twins.core.dto.rest.twin.TwinSortDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinMemberSortDTOReverseMapper extends RestSimpleDTOMapper<TwinSortDTOv1, TwinMemberSort> {
    @Override
    public void map(TwinSortDTOv1 src, TwinMemberSort dst, MapperContext mapperContext) throws Exception {
        dst.setFieldId(src.getTwinClassFieldId());
        dst.setDirection(src.getDirection());
    }
}
