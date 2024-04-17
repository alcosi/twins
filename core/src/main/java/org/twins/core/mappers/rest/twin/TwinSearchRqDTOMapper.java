package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinSearchRqDTOMapper extends RestSimpleDTOMapper<TwinSearchRqDTOv1, BasicSearch> {
    final TwinSearchDTOMapper twinSearchDTOMapper;

    @Override
    public void map(TwinSearchRqDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        twinSearchDTOMapper.map(src, dst, mapperContext);
        dst
                .setHeadSearch(twinSearchDTOMapper.convert(src.getHeadSearch(), mapperContext));
    }
}
