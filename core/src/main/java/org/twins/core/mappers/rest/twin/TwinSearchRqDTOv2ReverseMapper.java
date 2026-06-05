package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchExtendedDTOv2;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinSearchRqDTOv2ReverseMapper extends RestSimpleDTOMapper<TwinSearchRqDTOv2, BasicSearch> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    @Override
    public void map(TwinSearchRqDTOv2 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        TwinSearchExtendedDTOv2 searchDto = src.getSearch();
        if (searchDto != null) {
            twinSearchDTOReverseMapper.map(searchDto, dst, mapperContext);
            dst
                    .setHeadSearch(twinSearchDTOReverseMapper.convert(searchDto.getHeadSearch(), mapperContext))
                    .setChildrenSearch(twinSearchDTOReverseMapper.convert(searchDto.getChildrenSearch(), mapperContext));
        }
    }
}
