package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Request;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinClassFieldListRqV1")
public class TwinClassFieldListRqDTOv1 extends Request {
    @Schema(description = "id", example = DTOExamples.TWIN_CLASS_ID)
    public UUID twinClassId;
}
