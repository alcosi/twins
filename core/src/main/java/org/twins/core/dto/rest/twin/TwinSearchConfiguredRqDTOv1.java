package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.SearchConfiguredRqDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinSearchConfiguredRqV1")
public class TwinSearchConfiguredRqDTOv1 extends SearchConfiguredRqDTOv1 {
    @Schema(description = "search narrowing")
    public TwinSearchExtendedDTOv1 narrow;

    @Valid
    @Size(max = 2)
    @Schema(description = "Sort rules. Supports multi-field sort via TwinClassFieldId; list order defines precedence. Default: createdAt DESC")
    public List<TwinSortDTOv1> sorts;
}
