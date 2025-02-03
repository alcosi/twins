package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.LinkValidTwinsForNewTwinSearch;
import org.twins.core.dto.rest.link.LinkValidTwinsForNewSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.twin.TwinSearchSimpleDTOReverseMapper;

@Component
@RequiredArgsConstructor
public class LinkValidTwinsForNewTwinSearchDTOReverseMapper extends RestSimpleDTOMapper<LinkValidTwinsForNewSearchRqDTOv1, LinkValidTwinsForNewTwinSearch> {
    private final TwinSearchSimpleDTOReverseMapper twinSearchSimpleDTOReverseMapper;

    @Override
    public void map(LinkValidTwinsForNewSearchRqDTOv1 src, LinkValidTwinsForNewTwinSearch dst, MapperContext mapperContext) throws Exception {
        twinSearchSimpleDTOReverseMapper.map(src.getTwinSearch(), dst, mapperContext);
        dst
                .setNewTwinHeadTwinId(src.getHeadTwinId())
                .setNewTwinTwinClassId(src.getTwinClassId());
    }
}
