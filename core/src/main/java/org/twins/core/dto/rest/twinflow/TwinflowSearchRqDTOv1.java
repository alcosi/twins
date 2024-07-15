package org.twins.core.dto.rest.twinflow;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinflowListRqV1")
public class TwinflowSearchRqDTOv1 extends Request {
    @Schema(description = "twin class id list")
    public List<UUID> twinClassIdList;

    @Schema(description = "twin class id exclude list")
    public List<UUID> twinClassIdExcludeList;

    @Schema(description = "names list")
    public List<String> nameLikeList;

    @Schema(description = "description list")
    public List<String> descriptionLikeList;

    @Schema(description = "initial status id list")
    public List<UUID> initialStatusIdList;

    @Schema(description = "initial status id exclude list")
    public List<UUID> initialStatusIdExcludeList;
}
