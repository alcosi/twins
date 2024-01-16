package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchByLinkDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchRqDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import java.util.List;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class TwinSearchRqDTOMapper extends RestSimpleDTOMapper<TwinSearchRqDTOv1, BasicSearch> {

    @Override
    public void map(TwinSearchRqDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertSafe(src.getTwinClassIdList()))
                .setExtendsTwinClassIdList(convertSafe(src.getExtendsTwinClassIdList()))
                .setStatusIdList(convertSafe(src.getStatusIdList()))
                .setAssignerUserIdList(convertSafe(src.getAssignerUserIdList()))
                .setHeaderTwinIdList(convertSafe(src.getHeadTwinIdList()))
                .setTwinIdList(convertSafe(src.getTwinIdList()))
                .setTwinIdExcludeList(convertSafe(src.getTwinIdExcludeList()))
                .setTwinNameLikeList(convertSafe(src.getTwinNameLikeList()))
                .setCreatedByUserIdList(convertSafe(src.getCreatedByUserIdList()));
        if (src.getLinksList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByLinkDTO :  src.getLinksList()) {
                dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getDstTwinIdList());
            }
    }

    private <T> Set<T> convertSafe(List<T> list) {
        if (list == null)
            return null;
        return Set.copyOf(list);
    }

}
