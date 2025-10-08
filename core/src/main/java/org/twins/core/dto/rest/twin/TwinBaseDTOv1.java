package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name = "TwinBaseV1")
public class TwinBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID id;

    @Schema(description = "externalId", example = "934599502DFFAE")
    public String externalId;

    @Schema(description = "headTwinId", example = DTOExamples.HEAD_TWIN_ID)
    public UUID headTwinId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "description", example = "The biggest tree")
    public String description;

    @Schema(description = "statusId")
    public UUID statusId;

    @Schema(description = "class")
    public UUID twinClassId;

    @Schema(description = "current assigner")
    public UUID assignerUserId;

    @Schema(description = "author")
    public UUID authorUserId;

    @Schema(description = "owner business account id", example = DTOExamples.BUSINESS_ACCOUNT_ID)
    public UUID ownerBusinessAccountId;

    @Schema(description = "owner user id", example = DTOExamples.USER_ID)
    public UUID ownerUserId;

    @Schema(description = "page id")
    public UUID pageFaceId;

    @Schema(description = "breadcrumbs face id")
    public UUID breadCrumbsFaceId;

    @Schema(description = "freeze")
    public Boolean freeze;
}
