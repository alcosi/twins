package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.dto.rest.twinclass.TwinClassSearchDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.common.HierarchySearchRestDTOReverseMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinClassSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassSearchDTOv1, TwinClassSearch> {

    private final HierarchySearchRestDTOReverseMapper hierarchySearchRestDTOReverseMapper;

    @Override
    public void map(TwinClassSearchDTOv1 src, TwinClassSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertToSetSafe(src.getTwinClassIdExcludeList()))
                .setTwinClassKeyLikeList(convertToSetSafe(src.getTwinClassKeyLikeList()))
                .setExtendsHierarchyChildsForTwinClassSearch(hierarchySearchRestDTOReverseMapper.convert(src.getExtendsHierarchyChildsForTwinClassSearch()))
                .setExtendsHierarchyParentsForTwinClassSearch(hierarchySearchRestDTOReverseMapper.convert(src.getExtendsHierarchyParentsForTwinClassSearch()))
                .setHeadHierarchyChildsForTwinClassSearch(hierarchySearchRestDTOReverseMapper.convert(src.getHeadHierarchyChildsForTwinClassSearch()))
                .setHeadHierarchyParentsForTwinClassSearch(hierarchySearchRestDTOReverseMapper.convert(src.getHeadHierarchyParentsForTwinClassSearch()))
                .setOwnerTypeList(convertToSetSafe(src.getOwnerTypeList()))
                .setOwnerTypeExcludeList(convertToSetSafe(src.getOwnerTypeExcludeList()))
                .setCreatePermissionIdList(src.getCreatePermissionIdList())
                .setCreatePermissionIdExcludeList(src.getCreatePermissionIdExcludeList())
                .setEditPermissionIdList(src.getEditPermissionIdList())
                .setEditPermissionIdExcludeList(src.getEditPermissionIdExcludeList())
                .setDeletePermissionIdList(src.getDeletePermissionIdList())
                .setDeletePermissionIdExcludeList(src.getDeletePermissionIdExcludeList())
                .addOwnerTypeExclude()
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassKeyLikeList(convertToSetSafe(src.getTwinClassKeyLikeList()))
                .setNameI18nLikeList(convertToSetSafe(src.getNameI18nLikeList()))
                .setNameI18nNotLikeList(convertToSetSafe(src.getNameI18nNotLikeList()))
                .setDescriptionI18nLikeList(convertToSetSafe(src.getDescriptionI18nLikeList()))
                .setDescriptionI18nNotLikeList(convertToSetSafe(src.getDescriptionI18nNotLikeList()))
                .setMarkerDatalistIdList(src.getMarkerDatalistIdList())
                .setMarkerDatalistIdExcludeList(src.getMarkerDatalistIdExcludeList())
                .setTagDatalistIdList(src.getTagDatalistIdList())
                .setTagDatalistIdExcludeList(src.getTagDatalistIdExcludeList())
                .setFreezeIdList(src.getFreezeIdList())
                .setFreezeIdExcludeList(src.getFreezeIdExcludeList())
                .setAbstractt(src.getAbstractt())
                .setSegment(src.getSegment())
                .setHasSegments(src.getHasSegments())
                .setUniqueName(src.getUniqueName())
                .setTwinflowSchemaSpace(src.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(src.getTwinClassSchemaSpace())
                .setPermissionSchemaSpace(src.getPermissionSchemaSpace())
                .setAliasSpace(src.getAliasSpace())
                .setViewPermissionIdList(convertToSetSafe(src.getViewPermissionIdList()))
                .setViewPermissionIdExcludeList(convertToSetSafe(src.getViewPermissionIdExcludeList()))
                .setAssigneeRequired(src.getAssigneeRequired())
                .setExternalIdLikeList(src.getExternalIdLikeList())
                .setExternalIdNotLikeList(src.getExternalIdNotLikeList())
        ;
    }
}
