package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "TwinConditionV1")
public class TwinConditionDTOv1 {
    @Schema(description = "match all child twins")
    public Boolean matchAll;

    @Schema(description = "child twins")
    public List<TwinSearchDTOv1> childTwins;
}
