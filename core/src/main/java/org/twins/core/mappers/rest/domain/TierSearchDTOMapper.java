package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.controller.rest.annotation.MapperModeBinding;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dto.rest.domain.TierDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;
import org.twins.core.mappers.rest.mappercontext.modes.TierMode;

@Component
@RequiredArgsConstructor
@MapperModeBinding(modes = TierMode.class)
public class TierSearchDTOMapper extends RestSimpleDTOMapper<TierEntity, TierDTOv1> {

    @Override
    public void map(TierEntity src, TierDTOv1 dst, MapperContext mapperContext) throws Exception {
        switch (mapperContext.getModeOrUse(TierMode.DETAILED)) {
            case DETAILED:
                dst
                        .setId(src.getId())
                        .setPermissionSchemaId(src.getPermissionSchemaId())
                        .setTwinflowSchemaId(src.getTwinflowSchemaId())
                        .setTwinclassSchemaId(src.getTwinClassSchemaId())
                        .setName(src.getName())
                        .setDescription(src.getDescription())
                        .setCustom(src.isCustom())
                        .setAttachmentsStorageQuotaCount(src.getAttachmentsStorageQuotaSize())
                        .setAttachmentsStorageQuotaSize(src.getAttachmentsStorageQuotaSize())
                        .setUserCountQuota(src.getUserCountQuota())
                ;

                break;
            case SHORT:
                dst
                        .setId(src.getId())
                        .setName(src.getName())
                ;
                break;
        }
    }
}