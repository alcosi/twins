package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.HierarchySearch;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.dto.rest.twinclass.HierarchySearchDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class TwinClassSearchRestDTOReverseMapper extends RestSimpleDTOMapper<TwinClassSearchRqDTOv1, TwinClassSearch> {

    @Override
    public void map(TwinClassSearchRqDTOv1 src, TwinClassSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassIdExcludeList(convertToSetSafe(src.getTwinClassIdExcludeList()))
                .setTwinClassKeyLikeList(convertToSetSafe(src.getTwinClassKeyLikeList()))
                .setExtendsHierarchyChildsForTwinClassIdList(mapHierarchySearchDto(src.getExtendsHierarchyChildsForTwinClassIdList()))
                .setExtendsHierarchyParentsForTwinClassIdList(mapHierarchySearchDto(src.getExtendsHierarchyParentsForTwinClassIdList()))
                .setHeadHierarchyChildsForTwinClassIdList(mapHierarchySearchDto(src.getHeadHierarchyChildsForTwinClassIdList()))
                .setHeadHierarchyParentsForTwinClassIdList(mapHierarchySearchDto(src.getHeadHierarchyParentsForTwinClassIdList()))
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
                .setAbstractt(src.getAbstractt())
                .setTwinflowSchemaSpace(src.getTwinflowSchemaSpace())
                .setTwinClassSchemaSpace(src.getTwinClassSchemaSpace())
                .setPermissionSchemaSpace(src.getPermissionSchemaSpace())
                .setAliasSpace(src.getAliasSpace())
                .setViewPermissionIdList(convertToSetSafe(src.getViewPermissionIdList()))
                .setViewPermissionIdExcludeList(convertToSetSafe(src.getViewPermissionIdExcludeList()))
        ;
    }

    public HierarchySearch mapHierarchySearchDto(HierarchySearchDTOv1 dtoV1) {
        return new HierarchySearch(dtoV1.twinClassIdList, dtoV1.twinClassIdExcludeList, dtoV1.depth);
    }
}
