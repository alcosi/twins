package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinValidatorSearchRqV1")
public class TwinValidatorSearchRqDTOv1 extends Request {

    @Schema(description = "search")
    public TwinValidatorSearchDTOv1 search;
}
