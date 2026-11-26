package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchExtendedDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;


@Component
@RequiredArgsConstructor
public class TwinSearchExtendedDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchExtendedDTOv1, BasicSearch> {

    private final TwinSearchDTOReverseMapper twinSearchDTOReverseMapper;

    /**
     * {@link TwinSearchExtendedDTOv1#sorts} are not mapped on purpose: search objects are sort-free
     * (the same object drives count queries). Controllers convert sorts separately and pass them
     * as an explicit search execution parameter.
     */
    @Override
    public void map(TwinSearchExtendedDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        twinSearchDTOReverseMapper.map(src, dst, mapperContext);
        dst
                .setHeadSearch(twinSearchDTOReverseMapper.convert(src.getHeadSearch(), mapperContext))
                .setChildrenSearch(twinSearchDTOReverseMapper.convert(src.getChildrenSearch(), mapperContext))
        ;
    }
}
