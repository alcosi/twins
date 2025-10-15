package org.twins.core.dto.rest.draft;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOConfig;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.enums.draft.DraftStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "DraftBaseV1")
public class DraftDTOv1 {
    @Schema(description = "draft id")
    public UUID id;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "created by user")
    public UUID createdByrUserId;

    @Schema(description = "count of new twins")
    public Integer twinCreateCount;

    @Schema(description = "count of updated twins")
    public Integer twinUpdateCount;

    @Schema(description = "count of deleted twins")
    public Integer twinDeleteCount;

    @Schema(description = "count of deleted 'by status change' twins")
    public Integer twinDeletedByStatusCount;

    @Schema(description = "count of irrevocable deleted twins")
    public Integer twinDeletedIrrevocableCount;

    @Schema(description = "markers added count")
    public Integer twinMarkerCreateCount;

    @Schema(description = "markers deleted count")
    public Integer twinMarkerDeleteCount;

    @Schema(description = "tags added count")
    public Integer twinTagCreateCount;

    @Schema(description = "tags deleted count")
    public Integer twinTagDeleteCount;

    @Schema(description = "links created count")
    public Integer twinLinkCreateCount;

    @Schema(description = "links updated count")
    public Integer twinLinkUpdateCount;

    @Schema(description = "links deleted count")
    public Integer twinLinkDeleteCount;

    @Schema(description = "attachment created count")
    public Integer twinAttachmentCreateCount;

    @Schema(description = "attachment updated count")
    public Integer twinAttachmentUpdateCount;

    @Schema(description = "attachment deleted count")
    public Integer twinAttachmentDeleteCount;

    @Schema(description = "simple field created count")
    public Integer twinFieldSimpleCreateCount;

    @Schema(description = "simple field updated count")
    public Integer twinFieldSimpleUpdateCount;

    @Schema(description = "simple field deleted count")
    public Integer twinFieldSimpleDeleteCount;

    @Schema(description = "user field created count")
    public Integer twinFieldUserCreateCount;

    @Schema(description = "user field updated count")
    public Integer twinFieldUserUpdateCount;

    @Schema(description = "user field deleted count")
    public Integer twinFieldUserDeleteCount;

    @Schema(description = "data_list field created count")
    public Integer twinFieldDataListCreateCount;

    @Schema(description = "data_list field updated count")
    public Integer twinFieldDataListUpdateCount;

    @Schema(description = "data_list field deleted count")
    public Integer twinFieldDataListDeleteCount;

    @Schema(description = "draft status")
    public DraftStatus status;
}


