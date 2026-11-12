package org.twins.core.dto.rest.twin;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@Schema(name = "TwinGroupedSearchGroupV1")
public class TwinGroupedSearchGroupDTOv1 {
    @JsonUnwrapped
    @Schema(description = "Only the requested group field is present. A null value is the empty key")
    public TwinGroupKeyDTOv1 key;

    @Schema(description = "Number of distinct leaves in this subtree")
    public Long count;

    @Schema(description = "Nested groups. Present on every level except the last")
    public List<TwinGroupedSearchGroupDTOv1> groups;

    @Schema(description = "Leaf twin ids of the flat search, in member sort order. Present only on the last level. Full twins are in relatedObjects.twinMap when showTwinMode is set")
    public List<UUID> twinIdList;
}
