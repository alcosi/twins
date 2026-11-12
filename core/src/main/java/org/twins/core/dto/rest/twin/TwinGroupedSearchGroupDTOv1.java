package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(name = "TwinGroupedSearchGroupV1")
public class TwinGroupedSearchGroupDTOv1 {
    @JsonUnwrapped
    @Schema(description = "Only the requested group field is present. A null value is the empty key")
    public TwinGroupKeyDTOv1 key;

    @Schema(description = "Number of twins in this group. Equals twins.size()")
    public Long count;

    @Schema(description = "Twins of this group, in member sort order")
    public List<TwinDTOv2> twins;
}
