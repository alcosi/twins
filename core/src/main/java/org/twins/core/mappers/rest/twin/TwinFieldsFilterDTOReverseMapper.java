package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.exception.ErrorCodeCommon;
import org.cambium.common.exception.ServiceException;
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
        throw new ServiceException(ErrorCodeCommon.NOT_IMPLEMENTED);
    }

    @Override
    public TwinFieldFilter convert(TwinFieldsFilterDTOv1 src, MapperContext mapperContext) throws Exception {
        TwinFieldFilter twinFieldFilter = new TwinFieldFilter();
        twinFieldFilter
                .setClauses(twinFieldClauseDTOReverseMapper.convertCollection(src.getClauses(), mapperContext));
        return twinFieldFilter;
    }
}
