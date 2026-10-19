package org.twins.core.dto.rest.validator;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinValidatorDeleteRqV1")
public class TwinValidatorDeleteRqDTOv1 extends Request {

    @Schema(description = "twin validator id list to delete")
    public Set<UUID> twinValidatorIdList;

}
