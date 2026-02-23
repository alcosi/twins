package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSearchRqDTOv1;
import org.twins.core.mappers.rest.LongRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Deprecated
@Component
@RequiredArgsConstructor
public class TwinClassFieldSearchRqDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldSearchRqDTOv1, TwinClassFieldSearch> {

    private final LongRangeDTOReverseMapper longRangeDTOReverseMapper;

    @Override
    public void map(TwinClassFieldSearchRqDTOv1 src, TwinClassFieldSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setTwinClassIdMap(src.getTwinClassIdMap())
                .setTwinClassIdExcludeMap(src.getTwinClassIdExcludeMap())
                .setKeyLikeList(src.getKeyLikeList())
                .setKeyNotLikeList(src.getKeyNotLikeList())
                .setNameI18nLikeList(src.getNameI18nLikeList())
                .setNameI18nNotLikeList(src.getNameI18nNotLikeList())
                .setDescriptionI18nLikeList(src.getDescriptionI18nLikeList())
                .setDescriptionI18nNotLikeList(src.getDescriptionI18nNotLikeList())
                .setFieldTyperIdList(src.getFieldTyperIdList())
                .setFieldTyperIdExcludeList(src.getFieldTyperIdExcludeList())
                .setFieldInitiatorIdList(src.getFieldInitiatorIdList())
                .setFieldInitiatorIdExcludeList(src.getFieldInitiatorIdExcludeList())
                .setViewPermissionIdList(src.getViewPermissionIdList())
                .setViewPermissionIdExcludeList(src.getViewPermissionIdExcludeList())
                .setEditPermissionIdList(src.getEditPermissionIdList())
                .setEditPermissionIdExcludeList(src.getEditPermissionIdExcludeList())
                .setRequired(src.getRequired())
                .setExternalIdLikeList(src.getExternalIdLikeList())
                .setExternalIdNotLikeList(src.getExternalIdNotLikeList())
                .setOrderRange(longRangeDTOReverseMapper.convert(src.getOrderRange()));
    }
}
