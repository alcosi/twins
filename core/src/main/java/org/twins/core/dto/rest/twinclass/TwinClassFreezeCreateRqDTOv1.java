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
@Schema(name =  "TwinClassFreezeCreateRqV1")
public class TwinClassFreezeCreateRqDTOv1 extends Request {
    @Schema(description = "twin class freeze list")
    public List<TwinClassFreezeCreateDTOv1> twinClassFreezes;
}
