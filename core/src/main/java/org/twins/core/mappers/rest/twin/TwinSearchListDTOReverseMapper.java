package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearchList;
import org.twins.core.dto.rest.twin.TwinSearchListDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinSearchListDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchListDTOv1, BasicSearchList> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    @Override
    public void map(TwinSearchListDTOv1 src, BasicSearchList dst, MapperContext mapperContext) throws Exception {
            dst
                    .setSearches(twinSearchDTOReverseMapper.convertCollection(src.getSearches()))
                    .setMatchAll(src.getMatchAll() != null ? src.getMatchAll() : false);
    }
}
