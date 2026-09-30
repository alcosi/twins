package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
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
    @NotEmpty
    @Size(max = 50)
    @Schema(description = "twin status update list")
    public List<TwinStatusUpdateDTOv1> statuses;
}
