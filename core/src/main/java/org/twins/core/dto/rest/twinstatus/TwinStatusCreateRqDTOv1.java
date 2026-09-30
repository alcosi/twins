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
@Schema(name = "TwinStatusCreateRqV1")
public class TwinStatusCreateRqDTOv1 extends Request {

    @Valid
    @NotEmpty
    @Size(max = 50)
    @Schema(description = "twin status create list")
    public List<TwinStatusCreateDTOv1> statuses;
}
