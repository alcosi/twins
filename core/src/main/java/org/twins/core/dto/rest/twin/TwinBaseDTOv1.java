package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name = "TwinBaseV1")
public class TwinBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_ID)
    public UUID id;

    @Schema(description = "externalId", example = "934599502DFFAE")
    public String externalId;

    @RelatedObject(type = TwinDTOv2.class, name = "headTwin")
    @Schema(description = "headTwinId", example = DTOExamples.HEAD_TWIN_ID)
    public UUID headTwinId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "name", example = "Oak")
    public String name;

    @Schema(description = "description", example = "The biggest tree")
    public String description;

    @RelatedObject(type = TwinStatusDTOv1.class, name = "status")
    @Schema(description = "statusId")
    public UUID statusId;

    @RelatedObject(type = TwinClassDTOv1.class, name = "twinClass")
    @Schema(description = "class")
    public UUID twinClassId;

    @RelatedObject(type = UserDTOv1.class, name = "assigneeUser")
    @Schema(description = "current assigner")
    public UUID assignerUserId;

    @RelatedObject(type = UserDTOv1.class, name = "authorUser")
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

    @Schema(description = "aliases")
    public Set<String> aliases;
}
