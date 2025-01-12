package org.twins.core.mappers.rest.link;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.link.LinkEntity;
import org.twins.core.dao.link.LinkStrength;
import org.twins.core.domain.search.LinkSearch;
import org.twins.core.dto.rest.link.LinkSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

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
                .setTypeLikeList(safeConvertTypeLink(src.getTypeLikeList()))
                .setTypeNotLikeList(safeConvertTypeLink(src.getTypeNotLikeList()))
                .setStrengthLikeList(safeConvertStrengthLink(src.getStrengthLikeList()))
                .setStrengthNotLikeList(safeConvertStrengthLink(src.getStrengthNotLikeList()));
    }

    private Set<String> safeConvertTypeLink(Collection<LinkEntity.TwinlinkType> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }

    private Set<String> safeConvertStrengthLink(Collection<LinkStrength> list) {
        return list == null ? Collections.emptySet() : list.stream().map(Enum::name).collect(Collectors.toSet());
    }
}
