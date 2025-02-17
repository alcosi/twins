package org.twins.core.mappers.rest.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.twins.core.domain.tier.TierSave;
import org.twins.core.dto.rest.domain.TierSaveRqDTOv1;
import org.twins.core.mappers.rest.RestSimpleDTOMapper;
import org.twins.core.mappers.rest.mappercontext.MapperContext;

@Component
@RequiredArgsConstructor
public class TierSaveDTOReverseMapper extends RestSimpleDTOMapper<TierSaveRqDTOv1, TierSave> {

    @Override
    public void map(TierSaveRqDTOv1 src, TierSave dst, MapperContext mapperContext) throws Exception {
        dst
                .setId(src.getId())
                .setDomainId(src.getDomainId())
                .setName(src.getName())
                .setCustom(src.isCustom())
                .setPermissionSchemaId(src.getPermissionSchemaId())
                .setTwinflowSchemaId(src.getTwinflowSchemaId())
                .setTwinClassSchemaId(src.getTwinClassSchemaId())
                .setAttachmentsStorageQuotaCount(src.getAttachmentsStorageQuotaCount())
                .setAttachmentsStorageQuotaSize(src.getAttachmentsStorageQuotaSize())
                .setUserCountQuota(src.getUserCountQuota())
                .setDescription(src.getDescription());
    }
}