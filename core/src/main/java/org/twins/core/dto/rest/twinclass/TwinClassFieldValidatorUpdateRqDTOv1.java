package org.twins.core.dto.rest.twinclass;

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
@Schema(name = "TwinClassFieldValidatorUpdateRqV1")
public class TwinClassFieldValidatorUpdateRqDTOv1 extends Request {

    @Valid
    @Schema(description = "twin class field validator list")
    public List<TwinClassFieldValidatorUpdateDTOv1> validators;
}
