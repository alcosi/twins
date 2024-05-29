package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinSearchWithHeadV")
public class TwinSearchWithHeadDTOv1 extends TwinSearchDTOv1 {
    @Schema(description = "Head twin sub-search")
    public TwinSearchDTOv1 headSearch;

}
