package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchExtendedDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinSearchExtendedDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchExtendedDTOv1, BasicSearch> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;
    private final TwinSortDTOReverseMapper twinSortDTOReverseMapper;

    @Override
    public void map(TwinSearchExtendedDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        twinSearchDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setHeadSearch(twinSearchDTOReverseMapper.convert(src.getHeadSearch(), mapperContext))
                .setChildrenSearch(twinSearchDTOReverseMapper.convert(src.getChildrenSearch(), mapperContext))
                .setSorts(twinSortDTOReverseMapper.convertCollection(src.getSorts(), mapperContext))
        ;
    }
}
