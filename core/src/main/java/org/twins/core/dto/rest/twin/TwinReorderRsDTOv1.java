package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Response;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinReorderRsV1")
public class TwinReorderRsDTOv1 extends Response {
    @Schema(description = "all rows affected by the reorder moves")
    public List<TwinReorderItemDTOv1> items;
}
