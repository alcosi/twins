package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.SearchByAlias;
import org.twins.core.dto.rest.twin.TwinSearchByAliasRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;


@Component
@RequiredArgsConstructor
public class TwinSearchByAliasDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchByAliasRqDTOv1, SearchByAlias> {
    private final TwinSearchWithHeadDTOReverseMapper twinSearchWithHeadDTOReverseMapper;

    @Override
    public void map(TwinSearchByAliasRqDTOv1 src, SearchByAlias dst, MapperContext mapperContext) throws Exception {
        dst
                .setNarrow(twinSearchWithHeadDTOReverseMapper.convert(src.getNarrow(), mapperContext))
                .setParams(src.getParams());

    }
}
