package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.businessaccount.BusinessAccountDTOv1;
import org.twins.core.dto.rest.related.RelatedObject;

import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "UserToDomainAddV1")
public class UserToDomainAddDTOv1 {
    @Schema(description = "id", example = DTOExamples.USER_ID)
    public UUID id;

    @Schema(description = "businessAccountId", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    @RelatedObject(type = BusinessAccountDTOv1.class, name = "businessAccount")
    public UUID businessAccountId;

    @Schema(description = "domainId", example = DTOExamples.DOMAIN_ID)
    public UUID domainId;

    @Schema(description = "locale [optional]", example = DTOExamples.LOCALE)
    public String locale;

    @Schema(description = "fullName", example = "John Doe")
    public String fullName;

    @Schema(description = "email", example = "some@email.com")
    public String email;

    @Schema(description = "avatar url", example = "http://twins.org/a/avatar/carkikrefmkawfwfwg.png")
    public String avatar;
}
