package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twin.TwinDTOv2;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextTwinV1")
public class HistoryContextTwinDTOv1 implements HistoryContextDTO {
    public static final String KEY = "twinV1";
    public String contextType = KEY;

    @Schema(description = "From twin id", example = DTOExamples.TWIN_ID)
    @RelatedObject(type = TwinDTOv2.class, name = "fromTwin")
    public UUID fromTwinId;

    @Schema(description = "To twin id")
    @RelatedObject(type = TwinDTOv2.class, name = "toTwin")
    public UUID toTwinId;
}


