package org.twins.core.dto.rest.history;

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
@Schema(name = "HistoryBaseV1")
public class HistoryBaseDTOv1 {
    @Schema(description = "id", example = DTOExamples.TWIN_HISTORY_ID)
    public UUID id;

    @Schema(description = "twinId", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @JsonFormat(pattern = DTOConfig.DATE_FORMAT)
    @Schema(description = "created at", example = DTOExamples.INSTANT)
    public LocalDateTime createdAt;

    @Schema(description = "history actor id")
    public UUID actorUserId;

    @Schema()
    public HistoryType type;

    @Schema(description = "Filled only if type = fieldChanged")
    public String fieldName;
}
