package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinFieldFilter;
import org.twins.core.dto.rest.twin.TwinFieldsFilterDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFieldsFilterDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldsFilterDTOv1, TwinFieldFilter> {

    private final TwinFieldClauseDTOReverseMapper twinFieldClauseDTOReverseMapper;

    @Override
    public void map(TwinFieldsFilterDTOv1 src, TwinFieldFilter dst, MapperContext mapperContext) throws Exception {
        dst
                .setClauses(twinFieldClauseDTOReverseMapper.convertCollection(src.getClauses(), mapperContext));
    }

}
