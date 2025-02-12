package org.twins.core.domain.search;

import lombok.Data;
import lombok.experimental.Accessors;
import lombok.experimental.FieldNameConstants;
import org.cambium.common.util.Ternary;
import org.twins.core.dto.rest.LongRangeDTOv1;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@FieldNameConstants
public class TierSearch {
    private Set<UUID> idList;
    private Set<UUID> idExcludeList;
    private Set<UUID> permissionSchemaIdList;
    private Set<UUID> permissionSchemaIdExcludeList;
    private Set<UUID> twinflowSchemaIdList;
    private Set<UUID> twinflowSchemaIdExcludeList;
    private Set<UUID> twinclassSchemaIdList;
    private Set<UUID> twinclassSchemaIdExcludeList;
    private Set<String> nameLikeList;
    private Set<String> nameNotLikeList;
    private Set<String> descriptionLikeList;
    private Set<String> descriptionNotLikeList;
    private LongRangeDTOv1 attachmentsStorageQuotaCountRange;
    private LongRangeDTOv1 attachmentsStorageQuotaSizeRange;
    private LongRangeDTOv1 userCountQuotaRange;
    private Ternary custom;
}
