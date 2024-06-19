package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;

import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinStatusUpdateRqV1")
public class TwinStatusUpdateRqDTOv1 extends TwinStatusSaveRqDTOv1 {
    @Schema(description = "uuid", example = DTOExamples.TWIN_STATUS_ID)
    public UUID id;
}
