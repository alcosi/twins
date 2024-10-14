package org.twins.core.dto.rest.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "BusinessAccountUserV2")
public class BusinessAccountUserDTOv2 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "user id", example = DTOExamples.USER_ID)
    public UUID userId;

    @Schema(description = "user")
    public UserDTOv1 user;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID businessAccountId;

    @Schema(description = "business account")
    public BusinessAccountDTOv1 businessAccount;
}
