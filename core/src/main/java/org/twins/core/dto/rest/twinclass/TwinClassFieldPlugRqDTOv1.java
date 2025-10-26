package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldPlugRqV1")
public class TwinClassFieldPlugRqDTOv1 extends Request {

    @Schema(description = "fields to plug")
    private List<TwinClassFieldPlugDTOv1> fieldsToPlug;
}
