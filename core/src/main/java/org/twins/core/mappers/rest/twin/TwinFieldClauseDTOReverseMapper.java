package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.TwinFieldClause;
import org.twins.core.dto.rest.twin.TwinFieldClauseDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinFieldClauseDTOReverseMapper extends RestSimpleDTOMapper<TwinFieldClauseDTOv1, TwinFieldClause> {

    private final TwinFieldConditionListDTOReverseMapper twinFieldConditionListDTOReverseMapper;

    @Override
    public void map(TwinFieldClauseDTOv1 src, TwinFieldClause dst, MapperContext mapperContext) throws Exception {
        dst
                .setConditions(twinFieldConditionListDTOReverseMapper.convert(src.getConditions(), mapperContext));
    }

}
