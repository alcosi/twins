package org.twins.core.dto.rest.twinpointer;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinPointerListRsV1")
public class TwinPointerListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - twin pointer list")
    public List<TwinPointerDTOv1> twinPointers;
}
