package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.enums.sort.TwinValidatorSetGroupField;

import java.util.Set;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorSetCountRqV1")
public class TwinValidatorSetCountRqDTOv1 extends Request {
    @Valid
    @Schema(description = "search params")
    public TwinValidatorSetSearchDTOv1 search;

    @Size(max = 2)
    @Schema(description = "Group by fields")
    public Set<TwinValidatorSetGroupField> groupFields;
}
