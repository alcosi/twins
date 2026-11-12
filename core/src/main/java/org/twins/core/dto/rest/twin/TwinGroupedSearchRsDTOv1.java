package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinGroupedSearchRsV1")
public class TwinGroupedSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "Root groups. Leaves of the flat search sit on the last level")
    public List<TwinGroupedSearchGroupDTOv1> groups;
}
