package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.LinkValidTwinsForExistedTwinSearch;
import org.twins.core.dto.rest.link.LinkValidTwinsForNewSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinSearchSimpleDTOReverseMapper;

@Component
@RequiredArgsConstructor
public class LinkValidTwinsForExistedTwinSearchDTOReverseMapper extends RestSimpleDTOMapper<LinkValidTwinsForNewSearchRqDTOv1, LinkValidTwinsForExistedTwinSearch> {
    private final TwinSearchSimpleDTOReverseMapper twinSearchSimpleDTOReverseMapper;

    @Override
    public void map(LinkValidTwinsForNewSearchRqDTOv1 src, LinkValidTwinsForExistedTwinSearch dst, MapperContext mapperContext) throws Exception {
        twinSearchSimpleDTOReverseMapper.map(src.getTwinSearch(), dst, mapperContext);
    }
}
