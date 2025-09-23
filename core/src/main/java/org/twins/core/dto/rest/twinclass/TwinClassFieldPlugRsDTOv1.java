package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinClassFieldPlugRsV1")
public class TwinClassFieldPlugRsDTOv1 extends ResponseRelatedObjectsDTOv1 {

    @Schema(description = "successfully plugged fields")
    List<TwinClassFieldPlugDTOv1> pluggedFields;
}
