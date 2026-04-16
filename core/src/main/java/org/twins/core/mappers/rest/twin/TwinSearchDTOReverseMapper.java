package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.util.CollectionUtils;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.*;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.IntegerRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.HierarchySearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.util.ArrayList;
import java.util.List;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchDTOv1, BasicSearch> {

    private final TwinFieldsFilterDTOReverseMapper twinFieldsFilterDTOReverseMapper;
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;
    private final HierarchySearchRestDTOReverseMapper hierarchySearchRestDTOReverseMapper;
    private final IntegerRangeDTOReverseMapper integerRangeDTOReverseMapper;

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
                .setHierarchyChildrenSearch(hierarchySearchRestDTOReverseMapper.convert(src.getHierarchyChildrenSearch()))
                .setHeadHierarchyCounterDirectChildrenRange(integerRangeDTOReverseMapper.convert(src.getHeadHierarchyCounterDirectChildrenRange()))
                .setDistinct(src.getDistinct());
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
        if (CollectionUtils.isEmpty(src.getFields())) {
            dst
                    .setFieldsFilter(twinFieldsFilterDTOReverseMapper.convert(src.getFieldsFilter()));
        } else {
            //todo backward compatibility (cut me when transfer all filed on new impl)
            List<TwinFieldClauseDTOv1> clause = new ArrayList<>();
            for (var field : src.getFields().entrySet()) {
                clause
                        .add(new TwinFieldClauseDTOv1()
                                .or(new TwinFieldConditionDTOv1()
                                        .setTwinClassFieldId(field.getKey())
                                        .setTwinFieldSearch(field.getValue()))
                        );
            }
            dst
                    .setFieldsFilter(twinFieldsFilterDTOReverseMapper.convert(new TwinFieldsFilterDTOv1().setClauses(clause)));
        }
    }
}
