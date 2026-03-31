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
@Schema(name =  "TwinClassFreezeUpdateRqV1")
public class TwinClassFreezeUpdateRqDTOv1 extends Request {
    @Schema(description = "twin class freeze list")
    public List<TwinClassFreezeUpdateDTOv1> twinClassFreezes;
}
