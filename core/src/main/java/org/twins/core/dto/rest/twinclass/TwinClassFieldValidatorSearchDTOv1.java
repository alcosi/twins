package org.twins.core.dto.rest.twinclass;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinClassFieldValidatorSearchV1")
public class TwinClassFieldValidatorSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "twin class field id list")
    public Set<UUID> twinClassFieldIdList;

    @Schema(description = "twin class field id exclude list")
    public Set<UUID> twinClassFieldIdExcludeList;

    @Schema(description = "field validator featurer id list")
    public Set<Integer> fieldValidatorFeaturerIdList;

    @Schema(description = "field validator featurer id exclude list")
    public Set<Integer> fieldValidatorFeaturerIdExcludeList;
}
