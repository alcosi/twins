package org.twins.core.dto.rest.history;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.twin.TwinBaseDTOv2;
import org.twins.core.dto.rest.user.UserDTOv1;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(fluent = true)
@Schema(name = "HistoryBaseV2")
public class HistoryBaseDTOv2 extends HistoryBaseDTOv1 {
    @Schema(description = "twin")
    public TwinBaseDTOv2 twin;

    @Schema(description = "actor")
    public UserDTOv1 actorUser;
}
