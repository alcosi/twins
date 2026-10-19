package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.CountDTOv1;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.notification.NotificationSchemaDTOv1;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.tier.TierDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv1;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Schema(name = "DomainBusinessAccountCountV1")
public class DomainBusinessAccountCountDTOv1 extends CountDTOv1 {
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
    @RelatedObject(type = NotificationSchemaDTOv1.class, name = "notificationSchema")
    public UUID notificationSchemaId;
}
