package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassSearch;
import org.twins.core.dto.rest.twinclass.TwinClassSearchRqDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;

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
                .setHeadTwinClassIdList(convertToSetSafe(src.getHeadTwinClassIdList()))
                .setHeadTwinClassIdExcludeList(convertToSetSafe(src.getHeadTwinClassIdExcludeList()))
                .setExtendsTwinClassIdList(convertToSetSafe(src.getExtendsTwinClassIdList()))
                .setExtendsTwinClassIdExcludeList(convertToSetSafe(src.getExtendsTwinClassIdExcludeList()))
                .setOwnerTypeList(convertToSetSafe(src.getOwnerTypeList()))
                .setOwnerTypeExcludeList(convertToSetSafe(src.getOwnerTypeExcludeList()))
                .addOwnerTypeExclude()
                .setTwinClassIdList(convertToSetSafe(src.getTwinClassIdList()))
                .setTwinClassKeyLikeList(convertToSetSafe(src.getTwinClassKeyLikeList()))
                .setNameI18nLikeList(convertToSetSafe(src.getNameI18nLikeList()))
                .setNameI18nNotLikeList(convertToSetSafe(src.getNameI18nNotLikeList()))
                .setDescriptionI18nLikeList(convertToSetSafe(src.getDescriptionI18nLikeList()))
                .setDescriptionI18nNotLikeList(convertToSetSafe(src.getDescriptionI18nNotLikeList()))
                .setHeadTwinClassIdList(convertToSetSafe(src.getHeadTwinClassIdList()))
                .setExtendsTwinClassIdList(convertToSetSafe(src.getExtendsTwinClassIdList()))
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
}
