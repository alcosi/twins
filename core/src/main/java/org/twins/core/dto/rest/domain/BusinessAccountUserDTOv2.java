package org.twins.core.dto.rest.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "BusinessAccountUserV2")
public class BusinessAccountUserDTOv2 extends BusinessAccountUserDTOv1 {
    @Schema(description = "user")
    public UserDTOv1 user;

    @Schema(description = "business account")
    public BusinessAccountDTOv1 businessAccount;
}
