package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchByLinkDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import static org.cambium.common.util.CollectionUtils.convertSafe;

@Component
@RequiredArgsConstructor
public class TwinSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchDTOv1, BasicSearch> {

    @Override
    public void map(TwinSearchDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertSafe(src.getTwinClassIdExcludeList()))
                .setStatusIdList(convertSafe(src.getStatusIdList()))
                .setAssigneeUserIdList(convertSafe(src.getAssignerUserIdList()))
                .setAssigneeUserIdExcludeList(convertSafe(src.getAssignerUserIdExcludeList()))
                .setHeaderTwinIdList(convertSafe(src.getHeadTwinIdList()))
                .setHeadTwinClassIdList(convertSafe(src.getHeadTwinClassIdList()))
                .setExtendsTwinClassIdList(convertSafe(src.getExtendsTwinClassIdList()))
                .setTwinIdList(convertSafe(src.getTwinIdList()))
                .setTwinIdExcludeList(convertSafe(src.getTwinIdExcludeList()))
                .setTwinNameLikeList(convertSafe(src.getTwinNameLikeList()))
                .setCreatedByUserIdList(convertSafe(src.getCreatedByUserIdList()))
                .setCreatedByUserIdExcludeList(convertSafe(src.getCreatedByUserIdExcludeList()))
                .setHierarchyTreeContainsIdList(convertSafe(src.getHierarchyTreeContainsIdList()))
                .setStatusIdExcludeList(convertSafe(src.getStatusIdExcludeList()))
                .setTagDataListOptionIdList(convertSafe(src.getTagDataListOptionIdList()))
                .setTagDataListOptionIdExcludeList(convertSafe(src.getTagDataListOptionIdExcludeList()))
                .setMarkerDataListOptionIdList(convertSafe(src.getMarkerDataListOptionIdList()))
                .setMarkerDataListOptionIdExcludeList(convertSafe(src.getMarkerDataListOptionIdExcludeList()));
        if (src.getLinksList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByLinkDTO :  src.getLinksList()) {
                dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getDstTwinIdList(), false);
            }
        if (src.getNoLinksList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByNoLinkDTO : src.getNoLinksList()) {
                dst.addLinkDstTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getDstTwinIdList(), true);
            }
    }
}
