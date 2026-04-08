package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.enums.action.TwinAction;

@Data
@Accessors(chain = true)
@Schema(name = "TwinActionRestrictionV1")
public class TwinActionRestrictionDTO {
    @Schema(description = "Action that is restricted")
    public TwinAction action;

    @Schema(description = "Restriction type")
    public String type;

    @Schema(description = "Restriction description")
    public String description;
}
