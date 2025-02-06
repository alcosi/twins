package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(name = "TwinSearchSimpleV1")
public class TwinSearchSimpleDTOv1 {
    @Schema(description = "Twin name like")
    public String nameLike;

    @Schema(description = "Twin alias like")
    public String aliasLike;

}
