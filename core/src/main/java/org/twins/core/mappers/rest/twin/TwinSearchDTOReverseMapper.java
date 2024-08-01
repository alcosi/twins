package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchByLinkDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchDTOv1, BasicSearch> {

    @Override
    public void map(TwinSearchDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertToSetSafe(src.getTwinClassIdExcludeList()))
                .setStatusIdList(convertToSetSafe(src.getStatusIdList()))
                .setAssigneeUserIdList(convertToSetSafe(src.getAssignerUserIdList()))
                .setAssigneeUserIdExcludeList(convertToSetSafe(src.getAssignerUserIdExcludeList()))
                .setHeaderTwinIdList(convertToSetSafe(src.getHeadTwinIdList()))
                .setHeadTwinClassIdList(convertToSetSafe(src.getHeadTwinClassIdList()))
                .setExtendsTwinClassIdList(convertToSetSafe(src.getExtendsTwinClassIdList()))
                .setTwinIdList(convertToSetSafe(src.getTwinIdList()))
                .setTwinIdExcludeList(convertToSetSafe(src.getTwinIdExcludeList()))
                .setTwinNameLikeList(convertToSetSafe(src.getTwinNameLikeList()))
                .setCreatedByUserIdList(convertToSetSafe(src.getCreatedByUserIdList()))
                .setCreatedByUserIdExcludeList(convertToSetSafe(src.getCreatedByUserIdExcludeList()))
                .setHierarchyTreeContainsIdList(convertToSetSafe(src.getHierarchyTreeContainsIdList()))
                .setStatusIdExcludeList(convertToSetSafe(src.getStatusIdExcludeList()))
                .setTagDataListOptionIdList(convertToSetSafe(src.getTagDataListOptionIdList()))
                .setTagDataListOptionIdExcludeList(convertToSetSafe(src.getTagDataListOptionIdExcludeList()))
                .setMarkerDataListOptionIdList(convertToSetSafe(src.getMarkerDataListOptionIdList()))
                .setMarkerDataListOptionIdExcludeList(convertToSetSafe(src.getMarkerDataListOptionIdExcludeList()));
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
