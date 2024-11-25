package org.twins.core.dto.rest.space;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "SpaceRoleV2")
public class SpaceRoleDTOv2 extends SpaceRoleDTOv1 {
    @Schema(description = "twin class")
    public TwinClassDTOv1 twinClass;

    @Schema(description = "business account")
    public BusinessAccountDTOv1 businessAccount;
}
