package org.twins.core.mappers.rest.domainversion;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.search.DomainVersionAuditSearch;
import org.twins.core.dto.rest.domainversion.DomainVersionAuditSearchDTOv1;
import org.twins.core.mappers.rest.DataTimeRangeDTOReverseMapper;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class DomainVersionAuditSearchDTOReverseMapper extends RestSimpleDTOMapper<DomainVersionAuditSearchDTOv1, DomainVersionAuditSearch> {
    private final DataTimeRangeDTOReverseMapper dataTimeRangeDTOReverseMapper;

    @Override
    public void map(DomainVersionAuditSearchDTOv1 src, DomainVersionAuditSearch dst, MapperContext mapperContext) throws Exception {
        dst
                .setIdList(src.getIdList())
                .setIdExcludeList(src.getIdExcludeList())
                .setDomainVersionIdList(src.getDomainVersionIdList())
                .setDomainVersionIdExcludeList(src.getDomainVersionIdExcludeList())
                .setTableNameList(src.getTableNameList())
                .setTableNameExcludeList(src.getTableNameExcludeList())
                .setRowIdList(src.getRowIdList())
                .setRowIdExcludeList(src.getRowIdExcludeList())
                .setOperationList(src.getOperationList())
                .setOperationExcludeList(src.getOperationExcludeList())
                .setDomainOperationStatusList(src.getDomainOperationStatusList())
                .setDomainOperationStatusExcludeList(src.getDomainOperationStatusExcludeList())
                .setActorUserIdList(src.getActorUserIdList())
                .setActorUserIdExcludeList(src.getActorUserIdExcludeList())
                .setChangedAt(dataTimeRangeDTOReverseMapper.convert(src.getChangedAt()));
    }
}
