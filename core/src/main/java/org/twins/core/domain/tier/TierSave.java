package org.twins.core.domain.tier;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class TierSave {
    private UUID id;
    private UUID domainId;
    private String name;
    private boolean custom;
    private UUID permissionSchemaId;
    private UUID twinflowSchemaId;
    private UUID twinClassSchemaId;
    private Integer attachmentsStorageQuotaCount;
    private Long attachmentsStorageQuotaSize;
    private Integer userCountQuota;
    private String description;
}