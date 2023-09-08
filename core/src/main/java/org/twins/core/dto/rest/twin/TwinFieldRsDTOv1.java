package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.Response;

import java.util.UUID;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinFieldRsV1")
public class TwinFieldRsDTOv1 extends Response {
    @Schema(description = "twinId", example = DTOExamples.TWIN_ID)
    public UUID twinId;

    @Schema(description = "field data")
    public TwinFieldDTOv1 field;
}
