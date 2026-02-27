package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainBusinessAccountSearch;
import org.twins.core.dto.rest.domain.DomainBusinessAccountSearchRqDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

import java.sql.Timestamp;

import static org.cambium.common.util.CollectionUtils.convertToSetSafe;

@Component
@RequiredArgsConstructor
public class DomainBusinessAccountSearchRestDTOReverseMapper extends RestSimpleDTOMapper<DomainBusinessAccountSearchRqDTOv1, DomainBusinessAccountSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(DomainBusinessAccountSearchRqDTOv1 src, DomainBusinessAccountSearch dst, MapperContext mapperContext) throws Exception {
        dst
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
                .setTierIdList(convertToSetSafe(src.getTierIdList()))
                .setCreateAtRange(dataTimeRangeDTOReverseMapper.convert(src.getCreatedAt()));
    }
}
