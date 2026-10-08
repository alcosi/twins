package org.twins.core.dto.rest.link;

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
@Schema(name = "LinkValidatorCreateRqV1")
public class LinkValidatorCreateRqDTOv1 extends Request {

    @Valid
    @Schema(description = "link validator list")
    public List<LinkValidatorCreateDTOv1> linkValidators;
}
