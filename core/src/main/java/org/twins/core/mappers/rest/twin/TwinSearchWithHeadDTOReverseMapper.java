package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchWithHeadDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinSearchWithHeadDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchWithHeadDTOv1, BasicSearch> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    @Override
    public void map(TwinSearchWithHeadDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        twinSearchDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setHeadSearch(twinSearchDTOReverseMapper.convert(src.getHeadSearch(), mapperContext));
    }
}
