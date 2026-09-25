package org.twins.core.mappers.rest.tier;

import org.springframework.stereotype.Component;
import org.twins.core.dao.domain.TierEntity;
import org.twins.core.dto.rest.tier.TierUpdateDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
public class TierUpdateDTOReverseMapper extends RestSimpleDTOMapper<TierUpdateDTOv1, TierEntity> {

    @Override
    public void map(TierUpdateDTOv1 src, TierEntity dst, MapperContext mapperContext) throws Exception {
        dst
                .setName(src.getName())
                .setCustom(src.getCustom())
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setTwinflowSchemaId(src.getTwinflowSchemaId())
                .setTwinClassSchemaId(src.getTwinClassSchemaId())
                .setAttachmentsStorageQuotaCount(src.getAttachmentsStorageQuotaCount())
                .setAttachmentsStorageQuotaSize(src.getAttachmentsStorageQuotaSize())
                .setUserCountQuota(src.getUserCountQuota())
                .setDescription(src.getDescription());
    }
}
