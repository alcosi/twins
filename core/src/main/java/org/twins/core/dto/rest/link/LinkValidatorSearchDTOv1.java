package org.twins.core.dto.rest.link;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Set;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "LinkValidatorSearchV1")
public class LinkValidatorSearchDTOv1 {
    @Schema(description = "id list")
    public Set<UUID> idList;

    @Schema(description = "id exclude list")
    public Set<UUID> idExcludeList;

    @Schema(description = "link id list")
    public Set<UUID> linkIdList;

    @Schema(description = "link id exclude list")
    public Set<UUID> linkIdExcludeList;

    @Schema(description = "linker featurer id list")
    public Set<Integer> linkerFeaturerIdList;

    @Schema(description = "linker featurer id exclude list")
    public Set<Integer> linkerFeaturerIdExcludeList;
}
