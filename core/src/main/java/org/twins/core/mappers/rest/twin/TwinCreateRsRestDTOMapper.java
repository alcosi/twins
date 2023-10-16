package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinBusinessAccountAliasEntity;
import org.twins.core.dao.twin.TwinDomainAliasEntity;
import org.twins.core.dto.rest.twin.TwinCreateRsDTOv1;
import org.twins.core.mappers.rest.MapperContext;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.service.twin.TwinService;

import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class TwinCreateRsRestDTOMapper extends RestSimpleDTOMapper<TwinService.TwinCreateResult, TwinCreateRsDTOv1> {

    @Override
    public void map(TwinService.TwinCreateResult src, TwinCreateRsDTOv1 dst, MapperContext mapperContext) throws Exception {
        dst
                .setTwinId(src.getCreatedTwin().getId())
                .setBusinessAccountAliasList(src.getBusinessAccountAliasEntityList().stream().map(TwinBusinessAccountAliasEntity::getAlias).collect(Collectors.toList()))
                .setDomainAliasList(src.getDomainAliasEntityList().stream().map(TwinDomainAliasEntity::getAlias).collect(Collectors.toList()));
    }
}
