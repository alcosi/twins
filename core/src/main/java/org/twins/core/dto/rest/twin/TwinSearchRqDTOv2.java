package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(name = "TwinSearchRqV2")
public class TwinSearchRqDTOv2 extends Request {
    @Schema(description = "Search params")
    public TwinSearchExtendedDTOv2 search;

    @Size(max = 2)
    @Schema(description = "Sort rules. Supports multi-field sort via TwinClassFieldId")
    public List<TwinSortDTOv1> sorts;
}
