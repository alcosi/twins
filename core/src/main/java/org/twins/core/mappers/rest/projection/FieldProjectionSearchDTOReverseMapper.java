package org.twins.core.mappers.rest.projection;

import org.springframework.stereotype.Component;
import org.twins.core.domain.search.FieldProjectionSearch;
import org.twins.core.dto.rest.projection.FieldProjectionSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class FieldProjectionSearchDTOReverseMapper extends RestSimpleDTOMapper<FieldProjectionSearchDTOv1, FieldProjectionSearch> {
    @Override
    public void map(FieldProjectionSearchDTOv1 src, FieldProjectionSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setProjectionFieldSelector(src.getProjectionFieldSelector())
                .setSrcIdList(src.getSrcIdList())
                .setDstIdList(src.getDstIdList())
                .setProjectionTypeIdList(src.getProjectionTypeIdList());
    }
}
