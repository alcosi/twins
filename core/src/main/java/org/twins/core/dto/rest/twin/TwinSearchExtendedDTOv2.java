package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinSearchExtendedV2")
public class TwinSearchExtendedDTOv2 extends TwinSearchDTOv1 {
    @Schema(description = "Head twin sub-search")
    public TwinSearchDTOv1 headSearch;

    @Schema(description = "Children twin sub-search")
    public TwinSearchDTOv1 childrenSearch;
}
