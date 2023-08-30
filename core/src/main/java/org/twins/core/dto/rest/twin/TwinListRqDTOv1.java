package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;
import org.twins.core.dto.rest.tql.TqlDTOv1;

@Data
@Accessors(fluent = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinListRqV1")
public class TwinListRqDTOv1 extends Request {
    @Schema(description = "TQL")
    public TqlDTOv1 tql;

    @Schema(description = "show status details", defaultValue = "true", example = "true")
    public boolean showStatusDetails;

    @Schema(description = "show class details", defaultValue = "true", example = "true")
    public boolean showClassDetails;

    @Schema(description = "show user details", defaultValue = "true", example = "true")
    public boolean showUserDetails;
}
