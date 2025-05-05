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
@Schema(name = "TwinClassFieldBatchCreateRqV1")
public class TwinClassFieldBatchCreateRqDTOv1 extends Request {
    @Schema(description = "twin class field list")
    public List<TwinClassFieldCreateDTOv1> twinClassFields;
}
