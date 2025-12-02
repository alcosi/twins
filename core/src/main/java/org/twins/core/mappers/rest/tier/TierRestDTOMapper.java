package org.twins.core.mappers.rest.tier;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.controller.rest.annotation.MapperModePointerBinding;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dto.rest.tier.TierDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.PermissionSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.TierMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinClassSchemaMode;
import org.twins.core.mappers.rest.mappercontext.modes.TwinflowSchemaMode;
import org.twins.core.mappers.rest.permission.PermissionSchemaRestDTOMapper;
import org.twins.core.mappers.rest.twinclass.TwinClassSchemaDTOMapper;
import org.twins.core.mappers.rest.twinflow.TwinflowSchemaRestDTOMapper;

import static org.cambium.common.util.DateUtils.convertOrNull;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TierMode.class)
public class TierRestDTOMapper extends RestSimpleDTOMapper<TierEntity, TierDTOv1> {

    @MapperModePointerBinding(modes = PermissionSchemaMode.Tier2PermissionSchemaMode.class)
    private final PermissionSchemaRestDTOMapper permissionSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = TwinflowSchemaMode.Tier2TwinflowSchemaMode.class)
    private final TwinflowSchemaRestDTOMapper twinflowSchemaRestDTOMapper;

    @MapperModePointerBinding(modes = TwinClassSchemaMode.Tier2TwinClassSchemaMode.class)
    private final TwinClassSchemaDTOMapper twinclassSchemaDTOMapper;

    @Override
    public void map(TierEntity src, TierDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TierMode.DETAILED)) {
            case DETAILED -> dst
                    .setId(src.getId())
                    .setPermissionSchemaId(src.getPermissionSchemaId())
                    .setTwinflowSchemaId(src.getTwinflowSchemaId())
                    .setTwinClassSchemaId(src.getTwinClassSchemaId())
                    .setName(src.getName())
                    .setDescription(src.getDescription())
                    .setCustom(src.getCustom())
                    .setAttachmentsStorageQuotaCount(src.getAttachmentsStorageQuotaCount())
                    .setAttachmentsStorageQuotaSize(src.getAttachmentsStorageQuotaSize())
                    .setUserCountQuota(src.getUserCountQuota())
                    .setCreatedAt(convertOrNull(src.getCreatedAt()))
                    .setUpdatedAt(convertOrNull(src.getUpdatedAt()));

            case SHORT -> dst
                    .setId(src.getId())
                    .setName(src.getName());
        }

        if (mapperContext.hasModeButNot(PermissionSchemaMode.Tier2PermissionSchemaMode.HIDE)) {
            dst.setPermissionSchemaId(src.getPermissionSchemaId());
            permissionSchemaRestDTOMapper.postpone(src.getPermissionSchema(), mapperContext.forkOnPoint(PermissionSchemaMode.Tier2PermissionSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinflowSchemaMode.Tier2TwinflowSchemaMode.HIDE)) {
            dst.setTwinflowSchemaId(src.getTwinflowSchemaId());
            twinflowSchemaRestDTOMapper.postpone(src.getTwinflowSchema(), mapperContext.forkOnPoint(TwinflowSchemaMode.Tier2TwinflowSchemaMode.SHORT));
        }
        if (mapperContext.hasModeButNot(TwinClassSchemaMode.Tier2TwinClassSchemaMode.HIDE)) {
            dst.setTwinClassSchemaId(src.getTwinClassSchemaId());
            twinclassSchemaDTOMapper.postpone(src.getTwinClassSchema(), mapperContext.forkOnPoint(TwinClassSchemaMode.Tier2TwinClassSchemaMode.SHORT));
        }
    }
}
