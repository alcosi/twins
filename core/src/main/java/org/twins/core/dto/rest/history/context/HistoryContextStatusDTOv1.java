package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  HistoryContextStatusDTOv1.KEY, oneOf = { HistoryContextDTO.class })
public class HistoryContextStatusDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextStatusV1";

    public HistoryContextStatusDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "From status id", example = DTOExamples.TWIN_STATUS_ID)
    public UUID fromStatusId;

    @Schema(description = "From status")
    public TwinStatusDTOv1 fromStatus;

    @Schema(description = "To status id")
    public UUID toStatusId;

    @Schema(description = "To status", example = "")
    public TwinStatusDTOv1 toStatus;

}
