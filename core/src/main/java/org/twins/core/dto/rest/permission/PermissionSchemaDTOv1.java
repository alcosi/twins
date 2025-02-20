package org.twins.core.dto.rest.permission;

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
@Schema(name = "PermissionSchemaV1")
public class PermissionSchemaDTOv1 {
    @Schema(description = "id")
    public UUID id;

    @Schema(description = "domainId")
    public UUID domainId;

    @Schema(description = "businessAccountId")
    public UUID businessAccountId;

    @Schema(description = "name")
    public String name;

    @Schema(description = "description")
    public String description;

    @Schema(description = "createdByUserId")
    public UUID createdByUserId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;
}
