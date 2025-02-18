package org.twins.core.mappers.rest.tier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dto.rest.tier.TierDTOv2;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.*;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinclassSchemaDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaRestDTOMapper;

@Component
@RequiredArgsConstructor
public class TierRestDTOMapperV2 extends RestSimpleDTOMapper<TierEntity, TierDTOv2> {
    @MapperModePointerBinding(modes = TierMode.class)
    private final TierRestDTOMapper tierSearchDTOMapper;

    @MapperModePointerBinding(modes = PermissionSchemaMode.Tier2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = TwinflowSchemaMode.Tier2TwinflowSchemaMode.class)
    private final TwinflowSchemaRestDTOMapper twinflowSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = TwinclassSchemaMode.Tier2TwinclassSchemaMode.class)
    private final TwinclassSchemaDTOMapper twinclassSchemaDTOMapper;

    @Override
    public void map(TierEntity src, TierDTOv2 dst, MapperContext mapperContext) throws Exception {
        tierSearchDTOMapper.map(src, dst, mapperContext);
        if (mapperContext.hasModeButNot(PermissionSchemaMode.Tier2PermissionSchemaMode.HIDE))
            dst
                    .setPermissionSchema(permissionSchemaRestDTOMapper.convertOrPostpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.Tier2PermissionSchemaMode.SHORT)))
                    .setPermissionSchemaId(src.getPermissionSchemaId());
        if (mapperContext.hasModeButNot(TwinflowSchemaMode.Tier2TwinflowSchemaMode.HIDE))
            dst
                    .setTwinflowSchema(twinflowSchemaRestDTOMapper.convertOrPostpone(src.getTwinflowSchema(), mapperContext.forkOnPoint(TwinflowSchemaMode.Tier2TwinflowSchemaMode.SHORT)))
                    .setTwinflowSchemaId(src.getTwinflowSchemaId());
        if (mapperContext.hasModeButNot(TwinclassSchemaMode.Tier2TwinclassSchemaMode.HIDE))
            dst
                    .setTwinclassSchema(twinclassSchemaDTOMapper.convertOrPostpone(src.getTwinClassSchema(), mapperContext.forkOnPoint(TwinclassSchemaMode.Tier2TwinclassSchemaMode.SHORT)))
                    .setTwinclassSchemaId(src.getTwinClassSchemaId());
    }
}
