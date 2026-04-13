package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name =  "TwinClassFieldDuplicateRqV1")
public class TwinClassFieldDuplicateRqDTOv1 extends Request {
    @Schema(description = "duplicates list")
    public List<TwinClassFieldDuplicateDTOv1> duplicates;
}
