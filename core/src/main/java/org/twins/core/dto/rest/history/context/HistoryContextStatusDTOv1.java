package org.twins.core.dto.rest.history.context;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.related.RelatedObject;
import org.twins.core.dto.rest.twinstatus.TwinStatusDTOv1;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@Schema(name =  "HistoryContextStatusV1")
public class HistoryContextStatusDTOv1 implements HistoryContextDTO {
    public static final String KEY = "statusV1";
    public String contextType = KEY;

    @Schema(description = "From status id", example = DTOExamples.TWIN_STATUS_ID)
    @RelatedObject(type = TwinStatusDTOv1.class, name = "fromStatus")
    public UUID fromStatusId;

    @Schema(description = "To status id")
    @RelatedObject(type = TwinStatusDTOv1.class, name = "toStatus")
    public UUID toStatusId;
}


