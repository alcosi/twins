package org.twins.core.dto.rest.twinpointer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinPointerCreateRqV1")
public class TwinPointerCreateRqDTOv1 extends Request {
    @Schema(description = "twin pointers to create")
    public List<TwinPointerCreateDTOv1> twinPointers;
}
