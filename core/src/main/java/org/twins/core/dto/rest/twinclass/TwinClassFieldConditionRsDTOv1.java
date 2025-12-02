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
@Schema(name = "TwinClassFieldConditionRsV1")
public class TwinClassFieldConditionRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "conditions")
    public List<TwinClassFieldConditionDTOv1> conditions;
}
