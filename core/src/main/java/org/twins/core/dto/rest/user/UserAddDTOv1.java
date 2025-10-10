package org.twins.core.dto.rest.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "UserAddV1")
public class UserAddDTOv1 extends UserSaveDTOv1 {
    @Schema(description = "User ID")
    private UUID id;

    @Schema(description = "Business account ID", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    private UUID businessAccountId;

    @Schema(description = "Domain ID", example = DTOExamples.DOMAIN_ID)
    private UUID domainId;

    @Schema(description = "Locale [optional]", example = DTOExamples.LOCALE)
    private String locale;
}
