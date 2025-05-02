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
@Schema(name = "TwinClassFieldBatchUpdateRqV1")
public class TwinClassFieldBatchUpdateRqDTOv1 extends Request {
    @Schema(description = "twin class field list")
    public List<TwinClassFieldUpdateRqDTOv1> twinClassFields;
}
