package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.LinkSearch;
import org.twins.core.dto.rest.link.LinkSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class LinkSearchDTOReverseMapper extends RestSimpleDTOMapper<LinkSearchRqDTOv1, LinkSearch> {

    @Override
    public void map(LinkSearchRqDTOv1 src, LinkSearch dst, MapperContext mapperContext) {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setSrcTwinClassIdList(src.getSrcTwinClassIdList())
                .setSrcTwinClassIdExcludeList(src.getSrcTwinClassIdExcludeList())
                .setDstTwinClassIdList(src.getDstTwinClassIdList())
                .setDstTwinClassIdExcludeList(src.getDstTwinClassIdExcludeList())
                .setSrcOrDstTwinClassIdList(src.getSrcOrDstTwinClassIdList())
                .setSrcOrDstTwinClassIdExcludeList(src.getSrcOrDstTwinClassIdExcludeList())
                .setForwardNameLikeList(src.getForwardNameLikeList())
                .setForwardNameNotLikeList(src.getForwardNameNotLikeList())
                .setBackwardNameLikeList(src.getBackwardNameLikeList())
                .setBackwardNameNotLikeList(src.getBackwardNameNotLikeList())
                .setTypeLikeList(src.getTypeLikeList())
                .setTypeNotLikeList(src.getTypeNotLikeList())
                .setStrengthLikeList(src.getStrengthLikeList())
                .setStrengthNotLikeList(src.getStrengthNotLikeList());
    }
}
