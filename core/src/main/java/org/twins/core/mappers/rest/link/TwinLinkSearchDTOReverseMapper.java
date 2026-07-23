package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinLinkSearch;
import org.twins.core.dto.rest.link.TwinLinkSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TwinLinkSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinLinkSearchDTOv1, TwinLinkSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(TwinLinkSearchDTOv1 src, TwinLinkSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setSrcTwinIdList(src.getSrcTwinIdList())
                .setSrcTwinIdExcludeList(src.getSrcTwinIdExcludeList())
                .setDstTwinIdList(src.getDstTwinIdList())
                .setDstTwinIdExcludeList(src.getDstTwinIdExcludeList())
                .setSrcOrDstTwinIdList(src.getSrcOrDstTwinIdList())
                .setSrcOrDstTwinIdExcludeList(src.getSrcOrDstTwinIdExcludeList())
                .setLinkIdList(src.getLinkIdList())
                .setLinkIdExcludeList(src.getLinkIdExcludeList())
                .setCreatedByUserIdList(src.getCreatedByUserIdList())
                .setCreatedByUserIdExcludeList(src.getCreatedByUserIdExcludeList())
                .setCreatedAt(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()));
    }
}
