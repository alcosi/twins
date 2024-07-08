package org.twins.core.mappers.rest.twin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.dao.twin.TwinAliasEntity;
import org.twins.core.dto.rest.twin.TwinCreateRsDTOv1;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
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
                .setTwinAliasList(src.getTwinAliasEntityList().stream().map(TwinAliasEntity::getAlias).collect(Collectors.toList()));
    }
}
