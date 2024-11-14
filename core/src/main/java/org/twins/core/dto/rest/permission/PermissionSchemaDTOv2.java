package org.twins.core.dto.rest.permission;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "PermissionSchemaV2")
public class PermissionSchemaDTOv2 extends PermissionSchemaDTOv1 {
    @Schema(description = "businessAccount")
    public BusinessAccountDTOv1 businessAccount;

    @Schema(description = "createdByUser")
    public UserDTOv1 createdByUser;
}
