package org.twins.core.dto.rest.related;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.twinflow.TwinTransitionViewDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name =  "RelatedObjectsV1")
public class RelatedObjectsDTOv1 {
    @Schema(description = "related statuses map", example = "{twin map}")
    public Map<UUID, TwinDTOv2> twinMap;

    @Schema(description = "related statuses map", example = "{twin status map}")
    public Map<UUID, TwinStatusDTOv1> statusMap;

    @Schema(description = "related users map", example = "{user map}")
    public Map<UUID, UserDTOv1> userMap;

    @Schema(description = "related twinClass map", example = "{twin class map}")
    public Map<UUID, TwinClassDTOv1> twinClassMap;

    @Schema(description = "related transitionsMap map", example = "{twin transition map}")
    public Map<UUID, TwinTransitionViewDTOv1> transitionsMap;
}
