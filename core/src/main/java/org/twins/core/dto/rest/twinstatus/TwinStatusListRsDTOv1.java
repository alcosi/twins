package org.twins.core.dto.rest.twinstatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinStatusListRsV1")
public class TwinStatusListRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "results - status list")
    public List<TwinStatusDTOv1> statuses;
}
