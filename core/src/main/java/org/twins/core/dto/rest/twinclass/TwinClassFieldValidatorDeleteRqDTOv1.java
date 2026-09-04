package org.twins.core.dto.rest.twinclass;

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
@Schema(name = "TwinClassFieldValidatorDeleteRqV1")
public class TwinClassFieldValidatorDeleteRqDTOv1 extends Request {

    @Schema(description = "twin class field validator id list to delete")
    public Set<UUID> twinClassFieldValidatorIdList;
}
