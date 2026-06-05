package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchExtendedDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinSearchExtendedDTOv2ReverseMapper extends RestSimpleDTOMapper<TwinSearchExtendedDTOv2, BasicSearch> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    @Override
    public void map(TwinSearchExtendedDTOv2 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        twinSearchDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setHeadSearch(twinSearchDTOReverseMapper.convert(src.getHeadSearch(), mapperContext))
                .setChildrenSearch(twinSearchDTOReverseMapper.convert(src.getChildrenSearch(), mapperContext));
    }
}
