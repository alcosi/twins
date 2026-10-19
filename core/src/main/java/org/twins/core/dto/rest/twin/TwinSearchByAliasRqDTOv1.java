package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.Request;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name =  "TwinSearchByAliasRqV1")
public class TwinSearchByAliasRqDTOv1 extends Request {
    @Schema(description = "Search named params values")
    public Map<String, String> params;

    @Schema(description = "search narrowing")
    public TwinSearchExtendedDTOv1 narrow;

    @Schema(description = "Sort rules. Supports multi-field sort via TwinClassFieldId. Default: createdAt DESC")
    public List<TwinSortDTOv1> sorts;
}
