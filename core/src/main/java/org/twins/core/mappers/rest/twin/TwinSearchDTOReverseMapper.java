package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.cambium.common.kit.KitGrouped;
import org.cambium.common.util.MapUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twinclass.TwinClassFieldEntity;
import org.twins.core.domain.search.BasicSearch;
import org.twins.core.dto.rest.twin.TwinFieldSearchDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchByLinkDTOv1;
import org.twins.core.dto.rest.twin.TwinSearchDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twinclass.TwinClassFieldService;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinSearchDTOv1, BasicSearch> {

    private final TwinFieldSearchDTOReverseMapper twinFieldSearchDTOReverseMapper;
    @Lazy
    private final TwinClassFieldService twinClassFieldService;

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
                .setMarkerDataListOptionIdExcludeList(convertToSetSafe(src.getMarkerDataListOptionIdExcludeList()))
                .setTouchList(convertToSetSafe(src.getTouchList()))
                .setTouchExcludeList(convertToSetSafe(src.getTouchExcludeList()));
        if (src.getLinksAnyOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByLinkDTO : src.getLinksAnyOfList()) {
                dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getDstTwinIdList(), false, true);
            }
        if (src.getLinksNoAnyOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByNoLinkDTO : src.getLinksNoAnyOfList()) {
                dst.addLinkDstTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getDstTwinIdList(), true, true);
            }
        if (src.getLinksAllOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByLinkDTO : src.getLinksAllOfList()) {
                dst.addLinkDstTwinsId(twinSearchByLinkDTO.getLinkId(), twinSearchByLinkDTO.getDstTwinIdList(), false, false);
            }
        if (src.getLinksNoAllOfList() != null)
            for (TwinSearchByLinkDTOv1 twinSearchByNoLinkDTO : src.getLinksNoAllOfList()) {
                dst.addLinkDstTwinsId(twinSearchByNoLinkDTO.getLinkId(), twinSearchByNoLinkDTO.getDstTwinIdList(), true, false);
            }
        if (MapUtils.isNotEmpty(src.getFields())) {
            dst.setFields(new ArrayList<>());
            KitGrouped<TwinClassFieldEntity, UUID, UUID> twinClassFieldEntitiesKit = twinClassFieldService.findTwinClassFields(src.getFields().keySet());
            for (Map.Entry<UUID, TwinFieldSearchDTOv1> field : src.getFields().entrySet()) {
                dst.getFields().add(
                        twinFieldSearchDTOReverseMapper.convert(field.getValue(), mapperContext).setTwinClassFieldEntity(twinClassFieldEntitiesKit.get(field.getKey()))
                );
            }
        }
    }
}
