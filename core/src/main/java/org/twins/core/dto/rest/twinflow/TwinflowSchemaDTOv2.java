package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinflowSchemaV1")
public class TwinflowSchemaDTOv2 extends TwinflowSchemaDTOv1 {
    @Schema(description = "business account")
    public BusinessAccountDTOv1 businessAccount;

    @Schema(description = "created by user")
    public UserDTOv1 createdByUser;
}
