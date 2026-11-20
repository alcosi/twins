package org.twins.core.mappers.rest.twin;

import org.springframework.stereotype.Component;
import org.twins.core.domain.twin.TwinGroupLevel;
import org.twins.core.dto.rest.twin.TwinGroupLevelDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TwinGroupLevelDTOReverseMapper extends RestSimpleDTOMapper<TwinGroupLevelDTOv1, TwinGroupLevel> {
    @Override
    public void map(TwinGroupLevelDTOv1 src, TwinGroupLevel dst, MapperContext mapperContext) throws Exception {
        dst.setFieldId(src.getFieldId());
        dst.setSort(src.getSort());
    }
}
