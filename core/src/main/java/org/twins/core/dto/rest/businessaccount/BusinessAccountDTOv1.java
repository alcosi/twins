package org.twins.core.dto.rest.businessaccount;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "BusinessAccountV1")
public class BusinessAccountDTOv1 {
    @Schema(description = "id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID id;

    @Schema(description = "name", example = DTOExamples.BUSINESS_ACCOUNT_NAME)
    public String name;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;
}
