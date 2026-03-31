package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.dto.rest.domain.DomainBusinessAccountSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.IntegerRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class DomainBusinessAccountSearchRestDTOReverseMapper extends RestSimpleDTOMapper<DomainBusinessAccountSearchDTOv1, DomainBusinessAccountSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;
    private final IntegerRangeDTOReverseMapper integerRangeDTOReverseMapper;

    @Override
    public void map(DomainBusinessAccountSearchDTOv1 src, DomainBusinessAccountSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setBusinessAccountIdList(convertToSetSafe(src.getBusinessAccountIdList()))
                .setBusinessAccountIdExcludeList(convertToSetSafe(src.getBusinessAccountIdExcludeList()))
                .setBusinessAccountNameLikeList(convertToSetSafe(src.getBusinessAccountNameLikeList()))
                .setBusinessAccountNameNotLikeList(convertToSetSafe(src.getBusinessAccountNameNotLikeList()))
                .setPermissionSchemaIdList(convertToSetSafe(src.getPermissionSchemaIdList()))
                .setPermissionSchemaIdExcludeList(convertToSetSafe(src.getPermissionSchemaIdExcludeList()))
                .setTwinflowSchemaIdList(convertToSetSafe(src.getTwinflowSchemaIdList()))
                .setTwinflowSchemaIdExcludeList(convertToSetSafe(src.getTwinflowSchemaIdExcludeList()))
                .setTwinClassSchemaIdList(convertToSetSafe(src.getTwinClassSchemaIdList()))
                .setTwinClassSchemaIdExcludeList(convertToSetSafe(src.getTwinClassSchemaIdExcludeList()))
                .setNotificationSchemaIdList(convertToSetSafe(src.getNotificationSchemaIdList()))
                .setNotificationSchemaIdExcludeList(convertToSetSafe(src.getNotificationSchemaIdExcludeList()))
                .setTierIdList(convertToSetSafe(src.getTierIdList()))
                .setStorageUsedSizeRange(integerRangeDTOReverseMapper.convert(src.getStorageUsedSizeRange()))
                .setStorageUsedCountRange(integerRangeDTOReverseMapper.convert(src.getStorageUsedCountRange()))
                .setCreateAtRange(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()));
    }
}
