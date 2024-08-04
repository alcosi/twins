package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "TwinStarredListRsV1")
public class TwinStarredListRsDTOv1 extends Response {
    @Schema(description = "starred twins data")
    public List<TwinTouchDTOv1> starredTwins;
}
