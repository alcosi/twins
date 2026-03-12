package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.tier.TierDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "DomainBusinessAccountV1")
public class DomainBusinessAccountDTOv1 {

    @Schema(description = "domain business account id")
    public UUID id;

    @Schema(description = "business account id")
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @Schema(description = "", example = DTOExamples.PERMISSION_SCHEMA_ID)
    @RelatedObject(type = PermissionSchemaDTOv1.class, name = "permissionSchema")
    public UUID permissionSchemaId;

    @Schema(description = "", example = DTOExamples.TWINFLOW_SCHEMA_ID)
    @RelatedObject(type = TwinflowSchemaDTOv1.class, name = "twinflowSchema")
    public UUID twinflowSchemaId;

    @Schema(description = "", example = DTOExamples.TWIN_CLASS_SCHEMA_ID)
    @RelatedObject(type = TwinClassSchemaDTOv1.class, name = "twinClassSchema")
    public UUID twinClassSchemaId;

    @Schema(description = "business account tier id")
    @RelatedObject(type = TierDTOv1.class, name = "tier")
    public UUID tierId;

    @Schema(description = "")
    public UUID notificationSchemaId;

    @Schema(description = "")
    public Long attachmentsStorageUsedCount;

    @Schema(description = "used Storage space by files")
    public Long attachmentsStorageUsedSize;

    @Schema(description = "count of twins, belong to given businessAccount")
    public Long twinsCount;

    @Schema(description = "count of active users")
    public Long activeUsersCount;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;
}