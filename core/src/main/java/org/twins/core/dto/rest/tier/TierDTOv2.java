package org.twins.core.dto.rest.tier;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.permission.PermissionSchemaDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassSchemaDTOv1;
import org.twins.core.dto.rest.twinflow.TwinflowSchemaDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TierV2")
public class TierDTOv2 extends TierDTOv1 {
    @Schema(description = "permission schema")
    public PermissionSchemaDTOv1 permissionSchema;

    @Schema(description = "twinflow schema")
    public TwinflowSchemaDTOv1 twinflowSchema;

    @Schema(description = "twin class schema")
    public TwinClassSchemaDTOv1 twinClassSchema;
}