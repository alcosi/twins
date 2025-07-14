package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.twin.TwinBaseDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  HistoryContextTwinDTOv1.KEY, oneOf = { HistoryContextDTO.class })
public class HistoryContextTwinDTOv1 implements HistoryContextDTO {

    public static final String KEY = "HistoryContextTwinV1";

    public HistoryContextTwinDTOv1() {
        this.contextType = KEY;
    }

    @Schema(description = "Context type", allowableValues = {KEY}, example = KEY, requiredMode = Schema.RequiredMode.REQUIRED)
    public String contextType;

    @Schema(description = "From twin id", example = DTOExamples.TWIN_ID)
    public UUID fromTwinId;

    @Schema(description = "From twin")
    public TwinBaseDTOv1 fromTwin;

    @Schema(description = "To twin id")
    public UUID toTwinId;

    @Schema(description = "To twin")
    public TwinBaseDTOv1 toTwin;

}
