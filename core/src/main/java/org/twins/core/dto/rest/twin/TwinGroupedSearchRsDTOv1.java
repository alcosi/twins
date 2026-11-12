package org.twins.core.dto.rest.twin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.twins.core.dto.rest.DTOExamples;
import org.twins.core.dto.rest.ResponseRelatedObjectsDTOv1;

import java.util.List;
import java.util.UUID;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Schema(name = "TwinGroupedSearchRsV1")
public class TwinGroupedSearchRsDTOv1 extends ResponseRelatedObjectsDTOv1 {
    @Schema(description = "Field the groups are keyed by", example = DTOExamples.UUID_ID)
    public UUID groupFieldId;

    @Schema(description = "Every group of the filtered selection, each with all of its twins")
    public List<TwinGroupedSearchGroupDTOv1> groups;
}
