package org.twins.core.dto.rest.related;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;
import org.twins.core.dto.rest.twin.TwinDTOv2;
import org.twins.core.dto.rest.twin.TwinStatusDTOv1;
import org.twins.core.dto.rest.twinclass.TwinClassDTOv1;
import org.twins.core.dto.rest.user.UserDTOv1;

import java.util.Map;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "RelatedObjectsV1")
public class RelatedObjectsDTOv1 extends Response {
    @Schema(description = "results - statuses map")
    public Map<UUID, TwinDTOv2> twinMap;

    @Schema(description = "results - statuses map")
    public Map<UUID, TwinStatusDTOv1> statusMap;

    @Schema(description = "results - users map")
    public Map<UUID, UserDTOv1> userMap;

    @Schema(description = "results - twinClass map")
    public Map<UUID, TwinClassDTOv1> twinClassMap;
}
