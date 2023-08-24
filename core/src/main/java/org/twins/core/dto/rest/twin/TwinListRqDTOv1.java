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
    private TqlDTOv1 tql;
}
