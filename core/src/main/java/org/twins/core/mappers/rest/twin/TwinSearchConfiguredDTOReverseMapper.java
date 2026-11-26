package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.SearchByAlias;
import org.twins.core.dto.rest.twin.TwinSearchConfiguredRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TwinSearchConfiguredDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchConfiguredRqDTOv1, SearchByAlias> {
    private final TwinSearchExtendedDTOReverseMapper twinSearchExtendedDTOReverseMapper;
    private final TwinSortDTOReverseMapper twinSortDTOReverseMapper;

    @Override
    public void map(TwinSearchConfiguredRqDTOv1 src, SearchByAlias dst, MapperContext mapperContext) throws Exception {
        dst
                .setNarrow(twinSearchExtendedDTOReverseMapper.convert(src.getNarrow(), mapperContext))
                .setSorts(twinSortDTOReverseMapper.convertCollection(src.getSorts(), mapperContext))
                .setParams(src.getParams());

    }
}
