package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinClassFieldValidatorCreateRqV1")
public class TwinClassFieldValidatorCreateRqDTOv1 extends Request {

    @Schema(description = "twin class field validator list")
    public List<TwinClassFieldValidatorCreateDTOv1> validators;
}
