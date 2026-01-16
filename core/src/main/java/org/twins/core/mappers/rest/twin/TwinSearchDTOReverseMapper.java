package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinSearchByLinkDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchDTOv1, BasicSearch> {
    private final TwinFieldSearchMapDTOReverseMapper twinFieldSearchMapDTOReverseMapper;
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(TwinSearchDTOv1 src, BasicSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertToSetSafe(src.getTwinClassIdExcludeList()))
                .setStatusIdList(convertToSetSafe(src.getStatusIdList()))
                .setAssigneeUserIdList(convertToSetSafe(src.getAssignerUserIdList()))
                .setAssigneeUserIdExcludeList(convertToSetSafe(src.getAssignerUserIdExcludeList()))
                .setHeadTwinIdList(convertToSetSafe(src.getHeadTwinIdList()))
                .setHeadTwinClassIdList(convertToSetSafe(src.getHeadTwinClassIdList()))
                .setTwinClassExtendsHierarchyContainsIdList(convertToSetSafe(src.getTwinClassExtendsHierarchyContainsIdList()))
                .setTwinIdList(convertToSetSafe(src.getTwinIdList()))
                .setTwinIdExcludeList(convertToSetSafe(src.getTwinIdExcludeList()))
                .setExternalIdList(src.getExternalIdList())
                .setExternalIdExcludeList(src.getExternalIdExcludeList())
                .setTwinNameLikeList(convertToSetSafe(src.getTwinNameLikeList()))
                .setTwinNameNotLikeList(convertToSetSafe(src.getTwinNameNotLikeList()))
                .setTwinDescriptionLikeList(convertToSetSafe(src.getDescriptionLikeList()))
                .setTwinDescriptionNotLikeList(convertToSetSafe(src.getDescriptionNotLikeList()))
                .setCreatedByUserIdList(convertToSetSafe(src.getCreatedByUserIdList()))
                .setCreatedByUserIdExcludeList(convertToSetSafe(src.getCreatedByUserIdExcludeList()))
                .setHierarchyTreeContainsIdList(convertToSetSafe(src.getHierarchyTreeContainsIdList()))
                .setStatusIdExcludeList(convertToSetSafe(src.getStatusIdExcludeList()))
                .setTagDataListOptionIdList(convertToSetSafe(src.getTagDataListOptionIdList()))
                .setTagDataListOptionIdExcludeList(convertToSetSafe(src.getTagDataListOptionIdExcludeList()))
                .setMarkerDataListOptionIdList(convertToSetSafe(src.getMarkerDataListOptionIdList()))
                .setMarkerDataListOptionIdExcludeList(convertToSetSafe(src.getMarkerDataListOptionIdExcludeList()))
                .setTouchList(convertToSetSafe(src.getTouchList()))
                .setTouchExcludeList(convertToSetSafe(src.getTouchExcludeList()))
                .setCreatedAt(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()))
                .setMaxChildrenDepth(src.getMaxChildrenDepth())
                .setHierarchyPaths(src.getHierarchyPathSet());
        if (src.getLinksAnyOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByLinkDTO : src.getLinksAnyOfList()) {
                if (twinSearchByLinkDTO.isSrcElseDst()) {
                    dst.addLinkSrcTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getTwinIdList(), false, true);
                } else {
                    dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getTwinIdList(), false, true);
                    dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getDstTwinIdList(), false, true);
                }
            }
        if (src.getLinksNoAnyOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByNoLinkDTO : src.getLinksNoAnyOfList()) {
                if (twinSearchByNoLinkDTO.isSrcElseDst()) {
                    dst.addLinkSrcTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getTwinIdList(), true, true);
                } else {
                    dst.addLinkDstTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getTwinIdList(), true, true);
                    dst.addLinkDstTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getDstTwinIdList(), true, true);
                }
            }
        if (src.getLinksAllOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByLinkDTO : src.getLinksAllOfList()) {
                if (twinSearchByLinkDTO.isSrcElseDst()) {
                    dst.addLinkSrcTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getTwinIdList(), false, false);
                } else {
                    dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getTwinIdList(), false, false);
                    dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getDstTwinIdList(), false, false);
                }
            }
        if (src.getLinksNoAllOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByNoLinkDTO : src.getLinksNoAllOfList()) {
                if (twinSearchByNoLinkDTO.isSrcElseDst()) {
                    dst.addLinkSrcTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getTwinIdList(), true, false);
                } else {
                    dst.addLinkDstTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getTwinIdList(), true, false);
                    dst.addLinkDstTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getDstTwinIdList(), true, false);
                }
            }
        dst.setFields(twinFieldSearchMapDTOReverseMapper.convert(src.getFields()));
    }
}
