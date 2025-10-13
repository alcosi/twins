package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name =  "TwinBatchRsV1")
public class TwinBatchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "twin list")
    public List<TwinDTOv2> twinList;
}
