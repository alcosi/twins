package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinLinkSearch;
import org.twins.core.dto.rest.twin.TwinLinkSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinLinkSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkSearchRqDTOv1, TwinLinkSearch> {
    @Override
    public void map(TwinLinkSearchRqDTOv1 src, TwinLinkSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setSrcTwinIdList(src.getSrcTwinIdList())
                .setSrcTwinIdExcludeList(src.getSrcTwinIdExcludeList())
                .setDstTwinIdList(src.getDstTwinIdList())
                .setDstTwinIdExcludeList(src.getDstTwinIdExcludeList())
                .setLinkIdList(src.getLinkIdList())
                .setLinkIdExcludeList(src.getLinkIdExcludeList());
    }
}
