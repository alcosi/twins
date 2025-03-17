package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinBatchCreateRqV1")
public class TwinBatchCreateRqDTOv1 extends Request {
    @Schema(description = "twin list")
    public List<TwinCreateRqDTOv2> twins;
}
