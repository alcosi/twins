package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinStatusUpdateRqV1")
public class TwinStatusUpdateRqDTOv1 extends Request {

    @Valid
    @Schema(description = "twin status update list")
    public List<TwinStatusUpdateDTOv1> statuses;
}
