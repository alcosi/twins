package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "UserAddRqV1")
public class UserAddRqDTOv1 extends Request {
    @Deprecated
    @Schema(description = "businessAccountId", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;

    @Deprecated
    @Schema(description = "domainId", example = DTOExamples.DOMAIN_ID)
    public UUID domainId;

    @Schema(description = "user")
    public UserToDomainAddDTOv1 user;

    @Deprecated
    @Schema(description = "locale [optional]", example = DTOExamples.LOCALE)
    public String locale;
}
