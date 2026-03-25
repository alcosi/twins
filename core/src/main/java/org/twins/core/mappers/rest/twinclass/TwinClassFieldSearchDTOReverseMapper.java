package org.twins.core.mappers.rest.twinclass;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.TwinClassFieldSearch;
import org.twins.core.dto.rest.twinclass.TwinClassFieldSearchDTOv1;
import org.twins.core.mappers.rest.LongRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.projection.FieldProjectionSearchDTOReverseMapper;

@Component
@RequiredArgsConstructor
public class TwinClassFieldSearchDTOReverseMapper extends RestSimpleDTOMapper<TwinClassFieldSearchDTOv1, TwinClassFieldSearch> {

    private final LongRangeDTOReverseMapper longRangeDTOReverseMapper;
    private final FieldProjectionSearchDTOReverseMapper fieldProjectionSearchDTOReverseMapper;

    @Override
    public void map(TwinClassFieldSearchDTOv1 src, TwinClassFieldSearch dst, MapperContext mapperContext) throws Exception {
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
                .setTwinSorterIdList(src.getTwinSorterIdList())
                .setTwinSorterIdExcludeList(src.getTwinSorterIdExcludeList())
                .setViewPermissionIdList(src.getViewPermissionIdList())
                .setViewPermissionIdExcludeList(src.getViewPermissionIdExcludeList())
                .setEditPermissionIdList(src.getEditPermissionIdList())
                .setEditPermissionIdExcludeList(src.getEditPermissionIdExcludeList())
                .setRequired(src.getRequired())
                .setSystem(src.getSystem())
                .setExternalIdLikeList(src.getExternalIdLikeList())
                .setExternalIdNotLikeList(src.getExternalIdNotLikeList())
                .setOrderRange(longRangeDTOReverseMapper.convert(src.getOrderRange()))
                .setDependentField(src.getDependentField())
                .setHasDependentFields(src.getHasDependentFields())
                .setProjectionField(src.getProjectionField())
                .setHasProjectionFields(src.getHasProjectionFields())
                .setFieldProjectionSearch(fieldProjectionSearchDTOReverseMapper.convert(src.getFieldProjectionSearch()));
    }
}
